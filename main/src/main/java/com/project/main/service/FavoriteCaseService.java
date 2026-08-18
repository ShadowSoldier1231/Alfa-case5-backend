package com.project.main.service;



import com.project.main.dto.CasePublicDto;
import com.project.main.dto.FavoriteCaseDto;
import com.project.main.dto.PageResponse;

import com.project.main.enums.Difficulty;
import com.project.main.exception.BadRequestException;
import com.project.main.exception.ConflictException;
import com.project.main.exception.NotFoundException;
import com.project.main.model.UserFavoriteCase;
import com.project.main.repository.CaseRepository;
import com.project.main.repository.UserFavoriteCaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FavoriteCaseService {

    private final UserFavoriteCaseRepository favoriteCaseRepository;
    private final CaseRepository caseRepository;

    public FavoriteCaseService(UserFavoriteCaseRepository favoriteCaseRepository,
                               CaseRepository caseRepository){
        this.favoriteCaseRepository = favoriteCaseRepository;
        this.caseRepository = caseRepository;
    }

    public void addFavorite(Long userId, Long caseId){
        if(favoriteCaseRepository.existsByUserIdAndCaseId(userId, caseId)){
            throw new ConflictException("this case is already in your favourites");
        }
        caseRepository.findById(caseId).orElseThrow(() -> new NotFoundException("Case not found"));
        favoriteCaseRepository.save(new UserFavoriteCase(userId, caseId));
    }
    public void removeFavorite(Long userId, Long caseId){
        if(!favoriteCaseRepository.existsByUserIdAndCaseId(userId, caseId)){
            throw new BadRequestException("this case is not in your favourites");
        }

        favoriteCaseRepository.deleteByUserIdAndCaseId(userId, caseId);
    }


    public PageResponse<FavoriteCaseDto> getFavorites(Long userId, int page, int size, String search, String sort){
        if (page < 0) {
            throw new BadRequestException("Page cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }

        String searchTerm = null;

        if (search != null && !search.isBlank()) {
            searchTerm = escapeLikeWildcards(search.trim());
        }
        if (searchTerm != null && searchTerm.length() > 200) {
            throw new BadRequestException("Search query is too long");
        }
        Pageable pageable = PageRequest.of(page, size, buildCaseSort(sort));
        Page<Object[]> favoritesPage = favoriteCaseRepository.findFavoriteCases(searchTerm, userId, pageable);


        List<Long> caseIds = favoritesPage.getContent().stream()
                .map(row -> row[0] != null ? ((Number) row[0]).longValue() : null)
                .filter(Objects::nonNull)
                .toList();


        Map<Long, List<CasePublicDto.TagInfo>> tagsMap = loadTags(caseIds);


        List<FavoriteCaseDto> items = favoritesPage.getContent().stream()
                .map(row -> {
                    Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
                    String slug = row[1] != null ? row[1].toString() : null;
                    String title = row[2] != null ? row[2].toString() : null;
                    String titleEn = row[3] != null ? row[3].toString() : null;
                    String description = row[4] != null ? row[4].toString() : null;
                    String fullDescription = row[5] != null ? row[5].toString() : null;
                    Difficulty difficulty = row[6] != null ? Difficulty.valueOf(row[6].toString()) : null;

                    Integer averageSolveMin = row[7] != null ? ((Number) row[7]).intValue() : null;
                    String pdfUrl = row[8] != null ? row[8].toString() : null;
                    String iconUrl = row[9] != null ? row[9].toString() : null;
                    Integer viewsCount = row[10] != null ? ((Number) row[10]).intValue() : null;

                    LocalDateTime createdAt = toLocalDateTime(row[11]);
                    LocalDateTime updatedAt = toLocalDateTime(row[12]);
                    LocalDateTime addedAt = toLocalDateTime(row[13]);
                    List<CasePublicDto.TagInfo> tags = tagsMap.getOrDefault(id, List.of());

                    return new FavoriteCaseDto(
                            id, slug, title, titleEn, description, fullDescription,
                            difficulty, averageSolveMin, pdfUrl, iconUrl, viewsCount,
                            createdAt, updatedAt, addedAt, tags
                    );
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
        Sort sortBy = Sort.by(Sort.Direction.DESC, "created_at");

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

    private Boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof Boolean b) {
            return b;
        }

        return "true".equalsIgnoreCase(value.toString());
    }


    private String escapeLikeWildcards(String value) {
        if (value == null) {
            return null;
        }

        return value
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        return null;
    }

    private Map<Long, List<CasePublicDto.TagInfo>> loadTags(List<Long> cases) {
        if (cases.isEmpty()) return Map.of();
        return caseRepository.findTagsByCaseIds(cases).stream()
                .collect(Collectors.groupingBy(
                        row -> ((Number) row[0]).longValue(),
                        Collectors.mapping(
                                row -> new CasePublicDto.TagInfo(
                                        ((Number) row[1]).longValue(),
                                        (String) row[2],
                                        ((Number) row[3]).longValue()),
                                Collectors.toList())));
    }

}
