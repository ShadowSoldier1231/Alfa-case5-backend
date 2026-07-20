package com.project.main.service;


import com.project.main.dto.LeaderboardTopUser;
import com.project.main.model.*;
import com.project.main.repository.*;
import org.springframework.stereotype.Service;

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

    public Long GetCityIdByName(String cityName){
        City city = cityRepository.findByCityName(cityName).orElse(null);
        if(city == null){
            return -1L;
        }
        else return city.getId();

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

    public String getEmailById(Long userId){
        return userRepository.findById(userId)
                .map(UserSetup::getEmail)
                .orElse(null);
    }

    public UserData getUserData(Long userId){
        return userDataRepository.findById(userId).orElse(null);
    }

    public LeaderboardUser getLeaderboardUser(Long userId){
        return leaderboardRepository.findById(userId).orElse(null);
    }

    public byte[] getPictureById(Long id){
        UserAvatar data = avatarRepository.findById(id).orElse(null);
        if(data == null){
            return null;
        }
        return data.getPictureData();
    }

    public List<LeaderboardTopUser> getTop5Leaderboard() {

        List<LeaderboardUser> topRows = leaderboardRepository.findTop5ByOrderByScoreDescUserIdAsc();

        List<LeaderboardTopUser> result = new ArrayList<>();
        long currentPlacement = 1;

        for (LeaderboardUser row : topRows) {
            Long uId = row.getUserId();

            UserData uData = getUserData(uId);
            String firstName = (uData != null) ? uData.getFirstName() : "Unknown";
            String nickName = (uData != null) ? uData.getNickName() : "Unknown";

            Long cityId = (uData != null) ? uData.getCityId() : -1L;
            City city = getCityByCityId(cityId);
            String cityName = (city != null) ? city.getCityName() : "not_set";

            result.add(new LeaderboardTopUser(
                    uId,
                    currentPlacement++,
                    row.getScore(),
                    firstName,
                    nickName,
                    cityName
            ));
        }

        return result;
    }


}
