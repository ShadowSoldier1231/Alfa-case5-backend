package com.project.main.service.common;


import com.project.main.dto.leaderboard.LeaderboardInfo;
import com.project.main.dto.leaderboard.LeaderboardTopUser;
import com.project.main.dto.common.PageResponse;
import com.project.main.dto.user.UserProfile;
import com.project.main.enums.GenderCode;
import com.project.main.enums.UserStatus;
import com.project.main.exception.BadRequestException;
import com.project.main.model.common.City;
import com.project.main.model.user.LeaderboardUser;
import com.project.main.repository.common.CityRepository;
import com.project.main.repository.user.LeaderboardRepository;
import com.project.main.repository.user.UserDataRepository;
import com.project.main.repository.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
public class FetchingService {

    private final CityRepository cityRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final UserDataRepository userDataRepository;
    private final UserRepository userRepository;

    public FetchingService(LeaderboardRepository leaderboardRepository,
                           UserDataRepository userDataRepository, CityRepository cityRepository,
                           UserRepository userRepository) {


        this.userDataRepository = userDataRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
    }




    public boolean cityExistsById(Long cityId){
        if(cityId == -1){
            return false;
        }
        return cityRepository.existsById(cityId);
    }


    public PageResponse<City> searchCities(String cityName, int page, int size, String sort) {
        validatePagination(page, size);

        String searchTerm = cityName == null ? "" : escapeLikeWildcards(cityName.trim());

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

    public boolean userExistsById(Long userId){
        return userId > 0 ? userRepository.existsUserById(userId) : false;
    }

    public PageResponse<City> getAllCities(int page, int size, String search, String sort) {
        validatePagination(page, size);

        String searchTerm = null;
        if (search != null && !search.isBlank()) {
            searchTerm = search.trim();
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

        if (userEntry == null || userEntry.getScore() == 0) {

            Long total = leaderboardRepository.getTotalVerifiedUsersInLeaderboard();
            return new LeaderboardInfo(0L, total);
        }


        Long placement = leaderboardRepository.getGlobalUserPlacement(userId);
        Long total = leaderboardRepository.getTotalVerifiedUsersInLeaderboard();

        return new LeaderboardInfo(placement != null ? placement : 0L, total);
    }

    public LeaderboardInfo getLocalPlacementInfo(Long userId, Long caseId) {

        LeaderboardUser userEntry = leaderboardRepository.findById(userId).orElse(null);

        if (userEntry == null || userEntry.getScore() == 0) {

            Long total = leaderboardRepository.getTotalVerifiedUsersInLeaderboard();
            return new LeaderboardInfo(0L, total);
        }


        Long placement = leaderboardRepository.getUserPlacementInCase(caseId, userId);
        Long total = leaderboardRepository.getTotalVerifiedUsersInLeaderboard();

        return new LeaderboardInfo(placement != null ? placement : 0L, total);
    }

    public List<LeaderboardTopUser> getTop5Leaderboard() {
        List<Object[]> rows = leaderboardRepository.findTop5LeaderboardData();
        return buildTop5Result(rows);
    }

    public List<LeaderboardTopUser> getTop5LeaderboardByCase(Long caseId) {
        List<Object[]> rows = leaderboardRepository.findTop5LeaderboardDataByCaseId(caseId);
        return buildTop5Result(rows);
    }


    private List<LeaderboardTopUser> buildTop5Result(List<Object[]> rows) {
        List<LeaderboardTopUser> result = new ArrayList<>();
        long currentPlacement = 1;

        for (Object row : rows) {
            Object[] actualRow = unwrapRow(row);

            if (actualRow.length < 6) {
                continue;
            }

            Long uId = ((Number) actualRow[0]).longValue();
            Long score = ((Number) actualRow[1]).longValue();
            String firstName = safeString(actualRow[2]);
            String nickName = safeString(actualRow[3]);
            String cityName = safeString(actualRow[4]);
            String avatarKey = safeString(actualRow[5]);

            result.add(new LeaderboardTopUser(
                    uId,
                    currentPlacement++,
                    score,
                    (firstName != null && !firstName.isEmpty()) ? firstName : "Unknown",
                    (nickName != null && !nickName.isEmpty()) ? nickName : "Unknown",
                    (cityName != null && !cityName.isEmpty()) ? cityName : "not_set",
                    avatarKey
            ));
        }

        return result;
    }


    public UserProfile getBaseProfile(Long userId) {
        return userDataRepository.findProfileData(userId)
                .map(row -> {
                    Object[] actualRow = unwrapRow(row);
                    if (actualRow.length < 13) return null;

                    return UserProfile.builder()
                            .id(((Number) actualRow[0]).longValue())
                            .firstName(safeString(actualRow[1]))
                            .lastName(safeString(actualRow[2]))
                            .middleName(safeString(actualRow[3]))
                            .birthdate((LocalDate) actualRow[4])
                            .status(UserStatus.values()[((Number) actualRow[5]).intValue()])
                            .nickName(safeString(actualRow[6]))
                            .gender(GenderCode.values()[((Number) actualRow[7]).intValue()])
                            .score(actualRow[8] != null ? ((Number) actualRow[8]).longValue() : 0L)
                            .placement(actualRow[9] != null ? ((Number) actualRow[9]).longValue() : 0L)
                            .cityName(actualRow[10] != null ? safeString(actualRow[10]) : "not_set")
                            .regionName(actualRow[11] != null ? safeString(actualRow[11]) : "not_set")
                            .avatarUrl(safeString(actualRow[12]))
                            .build();
                })
                .orElse(null);
    }

    public UserProfile getMyProfile(Long userId) {
        return userDataRepository.findFullProfileData(userId)
                .map(row -> {
                    Object[] actualRow = unwrapRow(row);
                    if (actualRow.length < 14) return null;

                    return UserProfile.builder()
                            .id(((Number) actualRow[0]).longValue())
                            .firstName(safeString(actualRow[1]))
                            .lastName(safeString(actualRow[2]))
                            .middleName(safeString(actualRow[3]))
                            .birthdate((LocalDate) actualRow[4])
                            .status(UserStatus.values()[((Number) actualRow[5]).intValue()])
                            .nickName(safeString(actualRow[6]))
                            .gender(GenderCode.values()[((Number) actualRow[7]).intValue()])
                            .score(actualRow[8] != null ? ((Number) actualRow[8]).longValue() : 0L)
                            .placement(actualRow[9] != null ? ((Number) actualRow[9]).longValue() : 0L)
                            .cityName(actualRow[10] != null ? safeString(actualRow[10]) : "not_set")
                            .regionName(actualRow[11] != null ? safeString(actualRow[11]) : "not_set")
                            .email(safeString(actualRow[12]))
                            .avatarUrl(safeString(actualRow[13]))
                            .build();
                })
                .orElse(null);
    }

    public City getCityByUserId(long id) {
        return userDataRepository.findCityByUserId(id).orElse(null);
    }


    private Object[] unwrapRow(Object row) {
        if (row instanceof Object[] outerArray) {
            if (outerArray.length == 1 && outerArray[0] instanceof Object[] innerArray) {
                return innerArray;
            }
            return outerArray;
        }
        return new Object[0];
    }

    private String safeString(Object obj) {
        return obj != null ? obj.toString() : null;
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

    private String escapeLikeWildcards(String value) {
        if (value == null) {
            return null;
        }

        return value
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }

}
