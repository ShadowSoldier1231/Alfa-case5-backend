package com.project.main.service;


import com.project.main.model.City;
import com.project.main.model.UserData;
import com.project.main.repository.CityRepository;
import com.project.main.repository.LeaderboardRepository;
import com.project.main.repository.UserDataRepository;
import com.project.main.repository.UserRepository;
import org.springframework.stereotype.Service;


@Service
public class FetchingService {
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final UserDataRepository userDataRepository;


    public FetchingService(UserRepository userRepository,
                       LeaderboardRepository leaderboardRepository,
                       UserDataRepository userDataRepository, CityRepository cityRepository) {

        this.userRepository = userRepository;
        this.userDataRepository = userDataRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.cityRepository = cityRepository;
    }

    public Long GetCityIdByName(String cityName){
        City city = cityRepository.findByCityName(cityName).orElse(null);
        if(city == null){
            return -1L;
        }
        else return city.getId();

    }

    public City getCityByUserId(long id){

        UserData realUser = userDataRepository.findById(id).orElse(null);
        if(realUser == null){
            return null;
        }

        if (realUser.getCityId() == (long)-1){
            City city = new City();
            city.setCityName("not_set");
            city.setRegionName("not_set");
            return city;
        }

        City city = cityRepository.findById(realUser.getCityId()).orElse(null);

        if (city == null){
            city = new City();
            city.setCityName("error");
            city.setRegionName("error");
            return city;
        }

        return city;

    }

}
