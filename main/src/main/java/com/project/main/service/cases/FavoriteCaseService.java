package com.project.main.service.cases;



import com.project.main.dto.cases.CasePublicDto;
import com.project.main.dto.cases.FavoriteCaseDto;
import com.project.main.dto.common.PageResponse;
import com.project.main.exception.BadRequestException;
import com.project.main.exception.ConflictException;
import com.project.main.exception.NotFoundException;
import com.project.main.model.cases.CaseEntity;
import com.project.main.model.user.UserFavoriteCase;
import com.project.main.repository.cases.CaseRatingRepository;
import com.project.main.repository.cases.CaseRepository;
import com.project.main.repository.projection.CaseAverageRatingRow;
import com.project.main.repository.projection.CaseTagRow;
import com.project.main.repository.projection.FavoriteCaseRow;
import com.project.main.repository.user.UserFavoriteCaseRepository;
import com.project.main.service.component.TypeMapperComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FavoriteCaseService {

    private final UserFavoriteCaseRepository favoriteCaseRepository;
    private final CaseRepository caseRepository;
    private final CaseRatingRepository caseRatingRepository;
    private final TypeMapperComponent typeMapper;

    public FavoriteCaseService(UserFavoriteCaseRepository favoriteCaseRepository,
                               CaseRepository caseRepository,
                               CaseRatingRepository caseRatingRepository,
                               TypeMapperComponent typeMapper) {
        this.favoriteCaseRepository = favoriteCaseRepository;
        this.caseRepository = caseRepository;
        this.caseRatingRepository = caseRatingRepository;
        this.typeMapper = typeMapper;
    }

    @Transactional
    public void addFavorite(Long userId, Long caseId){
        if(favoriteCaseRepository.existsByUserIdAndCaseId(userId, caseId)){
            throw new ConflictException("this case is already in your favourites");
        }
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new NotFoundException("Case not found"));

        if (!Boolean.TRUE.equals(caseEntity.getActive())) {
            throw new BadRequestException("Case is not active");
        }

        favoriteCaseRepository.save(new UserFavoriteCase(userId, caseId));
    }


    @Transactional
    public void removeFavorite(Long userId, Long caseId){
        if(!favoriteCaseRepository.existsByUserIdAndCaseId(userId, caseId)){
            throw new BadRequestException("this case is not in your favourites");
        }

        favoriteCaseRepository.deleteByUserIdAndCaseId(userId, caseId);
    }


    public PageResponse<FavoriteCaseDto> getFavorites(Long userId, int page, int size, String search, String sort) {
        if (page < 0) {
            throw new BadRequestException("Page cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }

        String searchTerm = null;
        if (search != null && !search.isBlank()) {
            searchTerm = typeMapper.escapeLikeWildcards(search.trim());
        }

        if (searchTerm != null && searchTerm.length() > 200) {
            throw new BadRequestException("Search query is too long");
        }

        Pageable pageable = PageRequest.of(page, size, buildCaseSort(sort));

        Page<FavoriteCaseRow> favoritesPage =
                favoriteCaseRepository.findFavoriteCases(searchTerm, userId, pageable);

        List<Long> caseIds = favoritesPage.getContent().stream()
                .map(FavoriteCaseRow::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Double> ratingsMap = loadRatings(caseIds);
        Map<Long, List<CasePublicDto.TagInfo>> tagsMap = loadTags(caseIds);

        List<FavoriteCaseDto> items = favoritesPage.getContent().stream()
                .map(row -> {
                    List<CasePublicDto.TagInfo> tags =
                            tagsMap.getOrDefault(row.getId(), List.of());

                    FavoriteCaseDto dto = new FavoriteCaseDto(
                            row.getId(),
                            row.getSlug(),
                            row.getTitle(),
                            row.getTitle_en(),
                            row.getDescription(),
                            row.getFull_description(),
                            typeMapper.parseDifficulty(row.getDifficulty()),
                            row.getAverage_solve_min(),
                            row.getPdf_url(),
                            row.getIcon_url(),
                            row.getViews_count(),
                            row.getCreated_at(),
                            row.getUpdated_at(),
                            row.getAdded_at(),
                            tags
                    );

                    dto.setCaseRating(ratingsMap.get(row.getId()));

                    return dto;
                })
                .toList();

        return new PageResponse<>(
                items,
                favoritesPage.getNumber(),
                favoritesPage.getSize(),
                favoritesPage.getTotalElements(),
                favoritesPage.getTotalPages()
        );
    }


    private Sort buildCaseSort(String sort) {
        Sort sortBy = Sort.by(Sort.Direction.DESC, "added_at");

        if (sort == null || sort.isBlank()) {
            return sortBy;
        }

        String[] sortParts = sort.split(",");
        String property = sortParts[0].trim();

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1].trim())) {
            direction = Sort.Direction.DESC;
        }

        String sortColumn = switch (property.toLowerCase()) {
            case "id" -> "id";
            case "slug" -> "slug";
            case "title" -> "title";
            case "titleen", "title_en" -> "title_en";
            case "difficulty" -> "difficulty";
            case "full_description", "fullDescription", "fulldescription" -> "full_description";
            case "description" -> "description";
            case "averagesolvemin", "average_solve_min", "averagesolve", "average" -> "average_solve_min";
            case "views", "viewscount", "views_count" -> "views_count";
            case "active", "isactive", "is_active" -> "is_active";
            case "addedat", "added_at" -> "added_at";
            case "createdat", "created_at" -> "created_at";
            case "updatedat", "updated_at" -> "updated_at";
            default -> "added_at";
        };

        return Sort.by(direction, sortColumn);
    }

    private Map<Long, List<CasePublicDto.TagInfo>> loadTags(List<Long> caseIds) {
        if (caseIds.isEmpty()) {
            return Map.of();
        }

        return caseRepository.findTagsByCaseIds(caseIds).stream()
                .filter(row -> row.getCase_id() != null && row.getTag_id() != null)
                .collect(Collectors.groupingBy(
                        CaseTagRow::getCase_id,
                        Collectors.mapping(
                                row -> new CasePublicDto.TagInfo(
                                        row.getTag_id(),
                                        row.getTag_name(),
                                        row.getTag_case_count()
                                ),
                                Collectors.toList()
                        )
                ));
    }

    private Map<Long, Double> loadRatings(List<Long> caseIds) {
        if (caseIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Double> result = new HashMap<>();

        for (CaseAverageRatingRow row : caseRatingRepository.findAverageRatingsByCaseIds(caseIds)) {
            if (row.getCase_id() != null) {
                result.put(row.getCase_id(), row.getAvg_rating());
            }
        }

        return result;
    }

}
