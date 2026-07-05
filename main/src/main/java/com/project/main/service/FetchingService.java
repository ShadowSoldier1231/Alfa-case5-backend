package com.project.main.service;

import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.WebUser;
import com.project.main.model.City;
import com.project.main.model.UserData;
import com.project.main.model.Views;
import com.project.main.repository.CityRepository;
import com.project.main.repository.LeaderboardRepository;
import com.project.main.repository.UserDataRepository;
import com.project.main.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


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

    public WebUser getCityByUserId(long id){

        UserData realUser = userDataRepository.findById(id).orElse(null);
        if(realUser == null){
            return null;
        }
        WebUser webUser = new WebUser();

        if (realUser.getCityId() == (long)-1){
            webUser.setCityName("not_set");
            webUser.setRegionName("not_set");
            return webUser;
        }

        City city = cityRepository.findById(realUser.getCityId()).orElse(null);

        if (city == null){
            webUser.setCityName("error");
            webUser.setRegionName("error");
            return webUser;
        }

        webUser.setCityName(city.getCityName());
        webUser.setRegionName(city.getRegionName());
        return webUser;

    }

}
