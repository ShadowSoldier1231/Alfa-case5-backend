package com.project.main.service.common;


import com.project.main.dto.leaderboard.LeaderboardInfo;
import com.project.main.dto.leaderboard.LeaderboardTopUser;
import com.project.main.dto.common.PageResponse;
import com.project.main.dto.user.UserProfile;
import com.project.main.exception.BadRequestException;
import com.project.main.model.common.City;
import com.project.main.model.user.LeaderboardUser;
import com.project.main.repository.common.CityRepository;
import com.project.main.repository.projection.LeaderboardTopRow;
import com.project.main.repository.user.LeaderboardRepository;
import com.project.main.repository.user.UserDataRepository;
import com.project.main.repository.user.UserRepository;
import com.project.main.service.component.TypeMapperComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


@Service
public class FetchingService {

    private final CityRepository cityRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final UserDataRepository userDataRepository;
    private final UserRepository userRepository;
    private final TypeMapperComponent typeMapper;

    public FetchingService(LeaderboardRepository leaderboardRepository,
                           UserDataRepository userDataRepository,
                           CityRepository cityRepository,
                           UserRepository userRepository,
                           TypeMapperComponent typeMapper) {
        this.userDataRepository = userDataRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
        this.typeMapper = typeMapper;
    }




    public boolean cityExistsById(Long cityId){
        if(cityId == null){
            return false;
        }
        if(cityId == -1){
            return false;
        }
        return cityRepository.existsById(cityId);
    }


    public PageResponse<City> searchCities(String cityName, int page, int size, String sort) {
        validatePagination(page, size);

        String searchTerm = cityName == null ? "" : typeMapper.escapeLikeWildcards(cityName.trim());

        if (searchTerm.length() > 200) {
            throw new BadRequestException("Search query is too long");
        }

        if (searchTerm.isEmpty()) {
            return new PageResponse<>(
                    List.of(),
                    page,
                    size,
                    0,
                    0
            );
        }

        Pageable pageable = PageRequest.of(page, size, buildCitySort(sort));

        Page<City> cityPage = cityRepository.searchByCityName(searchTerm, pageable);

        return new PageResponse<>(
                cityPage.getContent(),
                cityPage.getNumber(),
                cityPage.getSize(),
                cityPage.getTotalElements(),
                cityPage.getTotalPages()
        );
    }

    public boolean userExistsById(Long userId) {
        return userId != null && userId > 0 && userRepository.existsUserById(userId);
    }

    public PageResponse<City> getAllCities(int page, int size, String search, String sort) {
        validatePagination(page, size);

        String searchTerm = null;
        if (search != null && !search.isBlank()) {
            searchTerm = typeMapper.escapeLikeWildcards(search.trim());
        }

        Pageable pageable = PageRequest.of(page, size, buildCitySort(sort));

        Page<City> cityPage = cityRepository.findAllCities(searchTerm, pageable);

        return new PageResponse<>(
                cityPage.getContent(),
                cityPage.getNumber(),
                cityPage.getSize(),
                cityPage.getTotalElements(),
                cityPage.getTotalPages()
        );
    }

    public LeaderboardInfo getGlobalPlacementInfo(Long userId) {

        LeaderboardUser userEntry = leaderboardRepository.findById(userId).orElse(null);
        Long score = userEntry != null ? userEntry.getScore() : null;

        if (score == null || score == 0L) {
            Long total = leaderboardRepository.getTotalVerifiedUsersInLeaderboard();
            return new LeaderboardInfo(0L, total);
        }

        Long placement = leaderboardRepository.getGlobalUserPlacement(userId);
        Long total = leaderboardRepository.getTotalVerifiedUsersInLeaderboard();

        return new LeaderboardInfo(placement != null ? placement : 0L, total);
    }

    public LeaderboardInfo getLocalPlacementInfo(Long userId, Long caseId) {

        LeaderboardUser userEntry = leaderboardRepository.findById(userId).orElse(null);
        Long score = userEntry != null ? userEntry.getScore() : null;

        if (score == null || score == 0L) {
            Long total = leaderboardRepository.getTotalVerifiedUsersInLeaderboard();
            return new LeaderboardInfo(0L, total);
        }


        Long placement = leaderboardRepository.getUserPlacementInCase(caseId, userId);
        Long total = leaderboardRepository.getTotalVerifiedUsersInLeaderboard();

        return new LeaderboardInfo(placement != null ? placement : 0L, total);
    }

    public List<LeaderboardTopUser> getTop5Leaderboard() {
        List<LeaderboardTopRow> rows = leaderboardRepository.findTop5LeaderboardData();
        return buildTop5Result(rows);
    }

    public List<LeaderboardTopUser> getTop5LeaderboardByCase(Long caseId) {
        List<LeaderboardTopRow> rows = leaderboardRepository.findTop5LeaderboardDataByCaseId(caseId);
        return buildTop5Result(rows);
    }


    private List<LeaderboardTopUser> buildTop5Result(List<LeaderboardTopRow> rows) {
        List<LeaderboardTopUser> result = new ArrayList<>();
        long currentPlacement = 1;

        for (LeaderboardTopRow row : rows) {
            result.add(new LeaderboardTopUser(
                    row.getUser_id(),
                    currentPlacement++,
                    row.getScore(),
                    row.getFirst_name() != null && !row.getFirst_name().isEmpty()
                            ? row.getFirst_name()
                            : "Unknown",
                    row.getNick_name() != null && !row.getNick_name().isEmpty()
                            ? row.getNick_name()
                            : "Unknown",
                    row.getCity_name() != null && !row.getCity_name().isEmpty()
                            ? row.getCity_name()
                            : "not_set",
                    row.getAvatar_url()
            ));
        }

        return result;
    }


    public UserProfile getBaseProfile(Long userId) {
        return userDataRepository.findProfileData(userId)
                .map(row -> UserProfile.builder()
                        .id(row.getId())
                        .firstName(row.getFirst_name())
                        .lastName(row.getLast_name())
                        .middleName(row.getMiddle_name())
                        .birthdate(row.getBirthdate())
                        .status(typeMapper.parseStatus(row.getStatus()))
                        .nickName(row.getNick_name())
                        .gender(typeMapper.parseGender(row.getGender()))
                        .score(row.getScore() != null ? row.getScore() : 0L)
                        .placement(row.getPlacement() != null ? row.getPlacement() : 0L)
                        .cityName(row.getCity_name() != null ? row.getCity_name() : "not_set")
                        .regionName(row.getRegion_name() != null ? row.getRegion_name() : "not_set")
                        .avatarUrl(row.getAvatar_url())
                        .build())
                .orElse(null);
    }

    public UserProfile getMyProfile(Long userId) {
        return userDataRepository.findFullProfileData(userId)
                .map(row -> UserProfile.builder()
                        .id(row.getId())
                        .firstName(row.getFirst_name())
                        .lastName(row.getLast_name())
                        .middleName(row.getMiddle_name())
                        .birthdate(row.getBirthdate())
                        .status(typeMapper.parseStatus(row.getStatus()))
                        .nickName(row.getNick_name())
                        .gender(typeMapper.parseGender(row.getGender()))
                        .score(row.getScore() != null ? row.getScore() : 0L)
                        .placement(row.getPlacement() != null ? row.getPlacement() : 0L)
                        .cityName(row.getCity_name() != null ? row.getCity_name() : "not_set")
                        .regionName(row.getRegion_name() != null ? row.getRegion_name() : "not_set")
                        .email(row.getEmail())
                        .avatarUrl(row.getAvatar_url())
                        .build())
                .orElse(null);
    }

    public City getCityByUserId(long id) {
        return userDataRepository.findCityByUserId(id).orElse(null);
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }
    }
    private Sort buildCitySort(String sort) {
        Sort sortBy = Sort.by(Sort.Direction.ASC, "city_name");

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
            case "cityname", "city_name", "name", "city" -> "city_name";
            case "regionname", "region_name", "region" -> "region_name";
            default -> "city_name";
        };

        return Sort.by(direction, sortColumn);
    }
    

    
}
