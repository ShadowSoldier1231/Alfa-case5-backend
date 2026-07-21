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

    private final CityRepository cityRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final UserDataRepository userDataRepository;
    private final  UserAvatarRepository avatarRepository;

    public FetchingService(LeaderboardRepository leaderboardRepository,
                           UserDataRepository userDataRepository, CityRepository cityRepository,
                           UserAvatarRepository avatarRepository) {


        this.userDataRepository = userDataRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.cityRepository = cityRepository;
        this.avatarRepository = avatarRepository;
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
                    Object[] actualRow = unwrapRow(row);
                    if (actualRow.length < 12) return null;

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
                            .build();
                })
                .orElse(null);
    }

    public UserProfile getMyProfile(Long userId) {
        return userDataRepository.findFullProfileData(userId)
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
                            .email(safeString(actualRow[12]))
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


}
