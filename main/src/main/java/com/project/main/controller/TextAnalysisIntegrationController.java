package com.project.main.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.RegisterResult;
import com.project.main.model.LeaderboardUser;

import com.project.main.model.Solution;
import com.project.main.model.UserSession;
import com.project.main.model.Views;
import com.project.main.repository.LeaderboardRepository;
import com.project.main.repository.SolutionRepository;
import com.project.main.repository.UserSessionRepository;
import com.project.main.service.SessionService;
import com.project.main.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/text/v1")
public class TextAnalysisIntegrationController  {

    private final UserSessionRepository sessionRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final UserService userService;
    private final SolutionRepository solutionRepository;
    private final SessionService sessionService;

    public TextAnalysisIntegrationController (UserSessionRepository userSessionRepository,
                           LeaderboardRepository leaderboardRepository, SessionService sessionService,
                                              UserService userService, SolutionRepository solutionRepository){

        this.sessionRepository = userSessionRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.userService = userService;
        this.solutionRepository = solutionRepository;
        this.sessionService = sessionService;
    }


    @JsonView(Views.RegisterResultPartial.class)
    @GetMapping("/checkCookie")
    public ResponseEntity<RegisterResult> checkCookie(@CookieValue(value = "token", required = false) String token){
        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getFirst();
        if(!cookieCheck.getSuccess()){
            return ResponseEntity.ok(cookieCheck);
        }
        return ResponseEntity.ok(cookieCheck);


    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/processViolation")
    public ResponseEntity<RegisterResult> processViolation(@CookieValue(value = "token", required = false) String token,
                                           HttpServletResponse response){
        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getFirst();
        if(!cookieCheck.getSuccess()){
            return ResponseEntity.ok(cookieCheck);
        }
        UserSession session = sessionPair.getSecond();

        LeaderboardUser user = leaderboardRepository.findById(session.getUserId())
                .orElse(null);
        if (user == null){
            return ResponseEntity.ok(new RegisterResult(false, "User does not exist"));
        }

        user.setWarningsCount(user.getWarningsCount()+1);
        leaderboardRepository.save(user);

        if(user.getWarningsCount() > 2){

            String errorText =  userService.banUser(user.getUserId());
            ResponseCookie cookie = ResponseCookie.from("token", "")
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok(new RegisterResult(false, errorText));
        }
        return ResponseEntity.ok(new RegisterResult(true, ""));

    }


    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/addScore")
    public ResponseEntity<RegisterResult> addScore(@CookieValue(value = "token", required = false) String token, @RequestBody Solution solution){
        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getFirst();
        if(!cookieCheck.getSuccess()){
            return ResponseEntity.ok(cookieCheck);
        }
        UserSession session = sessionPair.getSecond();

        LeaderboardUser user = leaderboardRepository.findById(session.getUserId())
                .orElse(null);
        if (user == null){
            return ResponseEntity.ok(new RegisterResult(false, "User does not exist"));
        }
        solution.setUserId(session.getUserId());
        solutionRepository.save(solution);

        Long totalScore = solutionRepository.getSumOfMaxRatingsByUserId(user.getUserId());
        user.setScore(totalScore != null ? totalScore : 0L);

        leaderboardRepository.save(user);
        return ResponseEntity.ok(new RegisterResult(true, ""));


    }

    @JsonView(Views.ChatView.class)
    @GetMapping("/getChatSequence/{caseId}")
    public ResponseEntity<List<Solution>> getChatSequence(@CookieValue(value = "token", required = false) String token,
                                          @PathVariable Long caseId) {

        if (token == null) return ResponseEntity.ok(Collections.emptyList());
        UserSession session = sessionRepository.findByToken(token)
                .orElse(null);
        if (session == null || session.getExpiryDate().isBefore(LocalDateTime.now())) {
            return  ResponseEntity.ok(Collections.emptyList());
        }

        return ResponseEntity.ok(solutionRepository.findByCaseIdAndUserIdOrderBySolutionIdAsc(caseId, session.getUserId()));
    }


}

