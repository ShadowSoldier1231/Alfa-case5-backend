package com.project.main.controller;



import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.LeadboardInfo;
import com.project.main.dto.LeaderboardTopUser;
import com.project.main.dto.RegisterResult;
import com.project.main.dto.UserProfile;
import com.project.main.model.City;

import com.project.main.model.UserSession;
import com.project.main.model.Views;
import com.project.main.repository.CityRepository;
import com.project.main.service.FetchingService;
import com.project.main.service.SessionService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/site")
public class WebApiController {

    private final FetchingService fetchingService;
    private final CityRepository cityRepository;
    private final SessionService sessionService;

    public WebApiController(CityRepository cityRepository, FetchingService fetchingService,
                            SessionService sessionService) {

        this.cityRepository = cityRepository;
        this.fetchingService = fetchingService;
        this.sessionService = sessionService;
    }


    @GetMapping("/user/{id}/city")
    public ResponseEntity<City> getCityName(@PathVariable long id) {
        City city = fetchingService.getCityByUserId(id);
        if (city == null) {
            return ResponseEntity.ok(new City(-1L, "not_set", "not_set"));
        }
        return ResponseEntity.ok(city);
    }


    @GetMapping("/searchLocation/{cityName}")
    public ResponseEntity<List<City>> getCityId(@PathVariable String cityName){
        return ResponseEntity.ok(cityRepository.findByCityNameContainingIgnoreCase(cityName));

    }

    @GetMapping("/getAllCities")
    public ResponseEntity<List<City>> getAllCities(){
        return ResponseEntity.ok(cityRepository.findAll());

    }

    @GetMapping("/leaderboard/case/{caseId}/top5")
    public ResponseEntity<List<LeaderboardTopUser>> getTop5ByCase(@PathVariable Long caseId) {
        return ResponseEntity.ok(fetchingService.getTop5LeaderboardByCase(caseId));
    }
    @GetMapping("/leaderboard/global/my-place")
    public ResponseEntity<LeadboardInfo> getMyGlobalPlace(@CookieValue(value = "token", required = false) String token) {


        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getLeft();
        if (!cookieCheck.getSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserSession session = sessionPair.getRight();


        Long userId = session.getUserId();

        LeadboardInfo info = fetchingService.getGlobalPlacementInfo(userId);

        return ResponseEntity.ok(info);
    }

    @GetMapping("/leaderboard/local/my-place/{caseId}")
    public ResponseEntity<LeadboardInfo> getMyLocalPlace(@CookieValue(value = "token", required = false) String token,
                                                         @PathVariable Long caseId) {


        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getLeft();
        if (!cookieCheck.getSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserSession session = sessionPair.getRight();


        Long userId = session.getUserId();

        LeadboardInfo info = fetchingService.getLocalPlacementInfo(userId, caseId);

        return ResponseEntity.ok(info);
    }

    @JsonView(Views.PublicProfile.class)
    @GetMapping("/user/{id}/profile")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable("id") Long userId) {
        if (userId == null || userId <= 0L) {
            return ResponseEntity.badRequest().build();
        }

        UserProfile profile = fetchingService.getBaseProfile(userId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(profile);
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




