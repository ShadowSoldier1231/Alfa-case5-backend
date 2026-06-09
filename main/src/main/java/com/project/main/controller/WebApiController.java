package com.project.main.controller;


import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.RegisterResult;
import com.project.main.dto.WebUser;
import com.project.main.model.City;
import com.project.main.model.UserData;
import com.project.main.model.Views;
import com.project.main.repository.AchievementRepository;
import com.project.main.repository.CityRepository;
import com.project.main.repository.UserDataRepository;
import com.project.main.repository.UserRepository;
import com.project.main.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/site")
public class WebApiController {
    private UserService userService;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;
    private final AchievementRepository achievementRepository;

    public WebApiController(CityRepository cityRepository, UserRepository userRepository,
                            UserService userService, UserDataRepository userDataRepository,
                            AchievementRepository achievementRepository) {

        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.userDataRepository = userDataRepository;
        this.achievementRepository = achievementRepository;
    }



    @JsonView(Views.CityView.class)
    @GetMapping("/user/{id}/city")
    public ResponseEntity<WebUser>  getCityName(@PathVariable long id){
        UserData realUser = userDataRepository.findById(id).orElse(null);
        if(realUser == null){
            return null;
        }
        WebUser webUser = new WebUser();
        webUser.setCityId(realUser.getCityId());

        if (webUser.getCityId() == (long)-1){
            webUser.setCityName("not_set");
            webUser.setRegionName("not_set");
            return ResponseEntity.ok(webUser);
        }

        City city = cityRepository.findById(webUser.getCityId()).orElse(null);
        if (city == null){
            webUser.setCityName("error");
            webUser.setRegionName("error");
            return ResponseEntity.ok(webUser);
        }
        webUser.setCityName(city.getCityName());
        webUser.setRegionName(city.getRegionName());
        return ResponseEntity.ok(webUser);

    }


    @GetMapping("/searchLocation/{cityName}")
    public ResponseEntity<List<City>> getCityId(@PathVariable String cityName){
        return ResponseEntity.ok(cityRepository.findByCityNameContainingIgnoreCase(cityName));

    }

    @GetMapping("/getAllCities")
    public ResponseEntity<List<City>> getAllCities(){
        return ResponseEntity.ok(cityRepository.findAll());

    }


}




