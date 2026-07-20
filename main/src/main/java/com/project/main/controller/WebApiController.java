package com.project.main.controller;



import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.LeaderboardTopUser;
import com.project.main.dto.UserProfile;
import com.project.main.model.City;

import com.project.main.model.UserData;
import com.project.main.model.Views;
import com.project.main.repository.CityRepository;
import com.project.main.service.FetchingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/site")
public class WebApiController {

    private final FetchingService fetchingService;
    private final CityRepository cityRepository;

    public WebApiController(CityRepository cityRepository, FetchingService fetchingService) {

        this.cityRepository = cityRepository;
        this.fetchingService = fetchingService;
    }



    @GetMapping("/user/{id}/city")
    public ResponseEntity<City>  getCityName(@PathVariable long id){
        return ResponseEntity.ok(fetchingService.getCityByUserId(id));

    }


    @GetMapping("/searchLocation/{cityName}")
    public ResponseEntity<List<City>> getCityId(@PathVariable String cityName){
        return ResponseEntity.ok(cityRepository.findByCityNameContainingIgnoreCase(cityName));

    }

    @GetMapping("/getAllCities")
    public ResponseEntity<List<City>> getAllCities(){
        return ResponseEntity.ok(cityRepository.findAll());

    }

    @JsonView(Views.PublicProfile.class)
    @GetMapping("/user/{id}/profile")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable("id") Long userId) {
        if (userId == null || userId <= 0L) {
            return ResponseEntity.badRequest().build();
        }
        UserData data = fetchingService.getUserData(userId);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
          UserProfile.builder()
                  .include(data)
                  .include(fetchingService.getLeaderboardUser(userId))
                  .include(fetchingService.getCityByCityId(data.getCityId()))
                  .build()
        );

    }

    @GetMapping("/user/{id}/avatar")
    public ResponseEntity<byte[]> getUserAvatar(@PathVariable("id") Long userId) {
        if (userId == null || userId <= 0L) {
            return ResponseEntity.badRequest().build();
        }

        byte[] imageBytes = fetchingService.getPictureById(userId);

        if (imageBytes == null || imageBytes.length == 0) {
            return ResponseEntity.notFound().build();
        }


        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .contentLength(imageBytes.length)
                .body(imageBytes);
    }

    @GetMapping("/leaderboard/top5")
    public ResponseEntity<List<LeaderboardTopUser>> getTop5Leaderboard() {
        List<LeaderboardTopUser> top5 = fetchingService.getTop5Leaderboard();

        if (top5 == null || top5.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(top5);
    }


}




