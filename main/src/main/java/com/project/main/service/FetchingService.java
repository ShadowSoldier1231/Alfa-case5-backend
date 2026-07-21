package com.project.main.service;


import com.project.main.dto.LeaderboardTopUser;
import com.project.main.dto.UserProfile;
import com.project.main.enums.GenderCode;
import com.project.main.enums.UserStatus;
import com.project.main.model.*;
import com.project.main.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
public class FetchingService {
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final UserDataRepository userDataRepository;
    private final  UserAvatarRepository avatarRepository;

    public FetchingService(UserRepository userRepository,
                           LeaderboardRepository leaderboardRepository,
                           UserDataRepository userDataRepository, CityRepository cityRepository,
                           UserAvatarRepository avatarRepository) {

        this.userRepository = userRepository;
        this.userDataRepository = userDataRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.cityRepository = cityRepository;
        this.avatarRepository = avatarRepository;
    }



    public City getCityByUserId(long id) {
        UserData realUser = userDataRepository.findById(id).orElse(null);
        if (realUser == null) {
            return null;
        }
        return getCityByCityId(realUser.getCityId());
    }

    public City getCityByCityId(Long cityId) {
        if (cityId == null || cityId == -1L) {
            return createStubCity("not_set");
        }
        return cityRepository.findById(cityId)
                .orElseGet(() -> createStubCity("error"));
    }


    private City createStubCity(String value) {
        City city = new City();
        city.setCityName(value);
        city.setRegionName(value);
        return city;
    }

    public boolean cityExistsById(Long cityId){
        if(cityId == -1){
            return false;
        }
        return cityRepository.existsById(cityId);
    }





    public byte[] getPictureById(Long id){
        UserAvatar data = avatarRepository.findById(id).orElse(null);
        if(data == null){
            return null;
        }
        return data.getPictureData();
    }

    public List<LeaderboardTopUser> getTop5Leaderboard() {
        List<Object[]> rows = leaderboardRepository.findTop5LeaderboardData();

        List<LeaderboardTopUser> result = new ArrayList<>();
        long currentPlacement = 1;

        for (Object[] row : rows) {
            Long uId = ((Number) row[0]).longValue();
            Long score = ((Number) row[1]).longValue();
            String firstName = (String) row[2];
            String nickName = (String) row[3];
            String cityName = (String) row[4];

            result.add(new LeaderboardTopUser(
                    uId,
                    currentPlacement++,
                    score,
                    firstName != null ? firstName : "Unknown",
                    nickName != null ? nickName : "Unknown",
                    cityName != null ? cityName : "not_set"
            ));
        }
        return result;
    }

    public UserProfile getBaseProfile(Long userId) {
        return userDataRepository.findProfileData(userId)
                .map(row -> {
                    String firstName = (String) row[0];
                    String lastName = (String) row[1];
                    String middleName = (String) row[2];
                    LocalDate birthdate = (LocalDate) row[3];
                    UserStatus status = (UserStatus) row[4];
                    String nickName = (String) row[5];
                    GenderCode gender = (GenderCode) row[6];
                    Long score = ((Number) row[7]).longValue();
                    Long placement = ((Number) row[8]).longValue();
                    String cityName = (String) row[9];
                    String regionName = (String) row[10];

                    return UserProfile.builder()
                            .firstName(firstName)
                            .lastName(lastName)
                            .middleName(middleName)
                            .birthdate(birthdate)
                            .status(status)
                            .nickName(nickName)
                            .gender(gender)
                            .score(score)
                            .placement(placement)
                            .cityName(cityName != null ? cityName : "not_set")
                            .regionName(regionName != null ? regionName : "not_set")
                            .build();
                })
                .orElse(null);
    }


    public UserProfile getMyProfile(Long userId) {
        return userDataRepository.findFullProfileData(userId)
                .map(row -> {
                    String firstName = (String) row[0];
                    String lastName = (String) row[1];
                    String middleName = (String) row[2];
                    LocalDate birthdate = (LocalDate) row[3];
                    UserStatus status = (UserStatus) row[4];
                    String nickName = (String) row[5];
                    GenderCode gender = (GenderCode) row[6];
                    Long score = ((Number) row[7]).longValue();
                    Long placement = ((Number) row[8]).longValue();
                    String cityName = (String) row[9];
                    String regionName = (String) row[10];
                    String email = (String) row[11];

                    return UserProfile.builder()
                            .firstName(firstName)
                            .lastName(lastName)
                            .middleName(middleName)
                            .birthdate(birthdate)
                            .status(status)
                            .nickName(nickName)
                            .gender(gender)
                            .score(score)
                            .placement(placement)
                            .cityName(cityName != null ? cityName : "not_set")
                            .regionName(regionName != null ? regionName : "not_set")
                            .email(email)
                            .build();
                })
                .orElse(null);
    }
}
