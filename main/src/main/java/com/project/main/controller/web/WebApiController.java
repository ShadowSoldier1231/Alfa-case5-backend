package com.project.main.controller.web;



import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.achievement.AchievementDto;
import com.project.main.dto.cases.FavoriteCaseDto;
import com.project.main.dto.common.PageResponse;
import com.project.main.dto.common.RegisterResult;
import com.project.main.dto.leaderboard.LeaderboardInfo;
import com.project.main.dto.leaderboard.LeaderboardTopUser;
import com.project.main.dto.user.UserPreferenceDto;
import com.project.main.dto.user.UserPreferenceUpdateRequest;
import com.project.main.dto.user.UserProfile;
import com.project.main.exception.BadRequestException;
import com.project.main.exception.InvalidSessionException;
import com.project.main.exception.NotFoundException;
import com.project.main.model.common.City;

import com.project.main.model.user.UserSession;
import com.project.main.model.common.Views;

import com.project.main.service.achievement.AchievementService;
import com.project.main.service.cases.FavoriteCaseService;
import com.project.main.service.common.FetchingService;
import com.project.main.service.auth.SessionService;
import com.project.main.service.user.UserPreferenceService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/site")
public class WebApiController {

    private final FetchingService fetchingService;
    private final FavoriteCaseService favoriteCaseService;
    private final SessionService sessionService;
    private final UserPreferenceService preferenceService;
    private final AchievementService achievementService;

    public WebApiController(FetchingService fetchingService,
                            SessionService sessionService,
                            FavoriteCaseService favoriteCaseService,
                            UserPreferenceService preferenceService,
                            AchievementService achievementService) {
        this.fetchingService = fetchingService;
        this.sessionService = sessionService;
        this.favoriteCaseService = favoriteCaseService;
        this.preferenceService = preferenceService;
        this.achievementService = achievementService;
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
    public ResponseEntity<PageResponse<City>> getCityId(
            @PathVariable String cityName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "city_name,asc") String sort) {

        return ResponseEntity.ok(fetchingService.searchCities(cityName, page, size, sort));
    }

    @GetMapping("/getAllCities")
    public ResponseEntity<PageResponse<City>> getAllCities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "city_name,asc") String sort) {

        return ResponseEntity.ok(fetchingService.getAllCities(page, size, search, sort));
    }

    @GetMapping("/leaderboard/case/{caseId}/top5")
    public ResponseEntity<List<LeaderboardTopUser>> getTop5ByCase(@PathVariable Long caseId) {
        return ResponseEntity.ok(fetchingService.getTop5LeaderboardByCase(caseId));
    }

    @GetMapping("/leaderboard/global/my-place")
    public ResponseEntity<LeaderboardInfo> getMyGlobalPlace(
            @CookieValue(value = "token", required = false) String token) {

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText(), token);
        }

        UserSession session = sessionPair.getRight();
        Long userId = session.getUserId();

        LeaderboardInfo info = fetchingService.getGlobalPlacementInfo(userId);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/leaderboard/local/my-place/{caseId}")
    public ResponseEntity<LeaderboardInfo> getMyLocalPlace(
            @CookieValue(value = "token", required = false) String token,
            @PathVariable Long caseId) {

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText(), token);
        }

        UserSession session = sessionPair.getRight();
        Long userId = session.getUserId();

        LeaderboardInfo info = fetchingService.getLocalPlacementInfo(userId, caseId);
        return ResponseEntity.ok(info);
    }

    @JsonView(Views.PublicProfile.class)
    @GetMapping("/user/{id}/profile")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable("id") Long userId) {
        if (userId == null || userId <= 0L) {
            throw new BadRequestException("Invalid user ID");
        }

        UserProfile profile = fetchingService.getBaseProfile(userId);
        if (profile == null) {
            throw new NotFoundException("Profile not found");
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

    @GetMapping("/me/achievements")
    public ResponseEntity<List<AchievementDto>> getMyAchievements(
            @CookieValue(value = "token", required = false) String token) {

        Long userId = sessionService.getUserIdOrThrow(token);

        List<AchievementDto> achievements = achievementService.getAchievementsForUser(userId);
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/{id}/achievements")
    public ResponseEntity<List<AchievementDto>> getUserAchievements(@PathVariable("id") Long userId) {
        if (userId == null || userId <= 0L) {
            throw new BadRequestException("Invalid user ID");
        }

        if (!fetchingService.userExistsById(userId)) {
            throw new NotFoundException("Invalid user ID");
        }

        List<AchievementDto> achievements = achievementService.getAchievementsForUser(userId);
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/me/favorites")
    public ResponseEntity<PageResponse<FavoriteCaseDto>> getMyFavourites(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "added_at,desc") String sort
    ){
        Long userId = sessionService.getUserIdOrThrow(token);
        return  ResponseEntity.ok(favoriteCaseService.getFavorites(userId, page, size, search, sort));

    }

    @PostMapping("/me/favorites/{caseId}")
    @JsonView(Views.RegisterResultPartial.class)
    public ResponseEntity<RegisterResult> addFavourite(@CookieValue(value = "token", required = false) String token,
                                                       @PathVariable("caseId") Long caseId){
        Long userId = sessionService.getUserIdOrThrow(token);
        favoriteCaseService.addFavorite(userId, caseId);
        return ResponseEntity.ok(new RegisterResult(true, "", userId));

    }

    @DeleteMapping("/me/favorites/{caseId}")
    @JsonView(Views.RegisterResultPartial.class)
    public ResponseEntity<RegisterResult> removeFavourite(@CookieValue(value = "token", required = false) String token,
                                                       @PathVariable("caseId") Long caseId){
        Long userId = sessionService.getUserIdOrThrow(token);
        favoriteCaseService.removeFavorite(userId, caseId);
        return ResponseEntity.ok(new RegisterResult(true, "", userId));

    }
    @GetMapping("/me/preferences")
    public ResponseEntity<UserPreferenceDto> getPreferences(@CookieValue(value = "token", required = false) String token) {
        
        Long userId = sessionService.getUserIdOrThrow(token);
        
        return ResponseEntity.ok(preferenceService.getPreferences(userId));
    }

    @PatchMapping("/me/preferences")
    public ResponseEntity<RegisterResult> updatePreferences(@CookieValue(value = "token", required = false) String token,
                                                            @RequestBody UserPreferenceUpdateRequest request) {

        Long userId = sessionService.getUserIdOrThrow(token);
        preferenceService.updatePreferences(request, userId);
        return ResponseEntity.ok(new RegisterResult(true, "", userId));
    }
}




