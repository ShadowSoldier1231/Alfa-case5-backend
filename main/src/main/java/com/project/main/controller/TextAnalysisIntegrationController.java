package com.project.main.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.RegisterResult;


import com.project.main.model.Solution;
import com.project.main.model.UserSession;
import com.project.main.model.Views;

import com.project.main.service.SessionService;
import com.project.main.service.SolutionService;
import com.project.main.service.UserModerationService;
import com.project.main.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;



@RestController
@RequestMapping("/api/text/v1")
public class TextAnalysisIntegrationController  {


    private final UserModerationService moderationService;
    private final UserService userService;
    private final SessionService sessionService;
    private final SolutionService solutionService;

    public TextAnalysisIntegrationController(UserModerationService moderationService,
                                             UserService userService, SolutionService solutionService,
                                             SessionService sessionService) {
        this.moderationService = moderationService;
        this.solutionService = solutionService;
        this.userService = userService;
        this.sessionService = sessionService;
    }


    @JsonView(Views.RegisterResultPartial.class)
    @GetMapping("/checkCookie")
    public ResponseEntity<RegisterResult> checkCookie(@CookieValue(value = "token", required = false) String token) {
        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        return ResponseEntity.ok(sessionPair.getFirst());
    }


    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/processViolation")
    public ResponseEntity<RegisterResult> processViolation(@CookieValue(value = "token", required = false) String token,
                                                           HttpServletResponse response) {
        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getFirst();
        if (!cookieCheck.getSuccess()) {
            return ResponseEntity.ok(cookieCheck);
        }
        UserSession session = sessionPair.getSecond();

        try {

            String banReason = moderationService.addWarning(session.getUserId());

            if (banReason != null) {

                sessionService.deleteAllSessions(session.getUserId());
                ResponseCookie cookie = sessionService.deleteCookie(token, false);
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                return ResponseEntity.ok(new RegisterResult(false, banReason, session.getUserId()));
            }

            return ResponseEntity.ok(new RegisterResult(true, "", session.getUserId()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new RegisterResult(false, e.getMessage(), session.getUserId()));
        }
    }


    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/addScore")
    public ResponseEntity<RegisterResult> addScore(@CookieValue(value = "token", required = false) String token,
                                                   @RequestBody Solution solution) {
        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getFirst();
        if (!cookieCheck.getSuccess()) {
            return ResponseEntity.ok(cookieCheck);
        }
        UserSession session = sessionPair.getSecond();

        if (solution.getCaseId() == null || solution.getSolutionText() == null ||
                solution.getSolutionResponse() == null || solution.getRating() == null) {
            return ResponseEntity.ok(new RegisterResult(false, "Invalid request", session.getUserId()));
        }
        if (solution.getSolutionText().isBlank() || solution.getSolutionResponse().isBlank()) {
            return ResponseEntity.ok(new RegisterResult(false, "Invalid request", session.getUserId()));
        }

        try {

            solutionService.submitSolution(session.getUserId(), solution);
            return ResponseEntity.ok(new RegisterResult(true, "", session.getUserId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new RegisterResult(false, e.getMessage(), session.getUserId()));
        }
    }

    @JsonView(Views.ChatView.class)
    @GetMapping("/getChatSequence/{caseId}")
    public ResponseEntity<List<Solution>> getChatSequence(@CookieValue(value = "token", required = false) String token,
                                                          @PathVariable Long caseId) {
        if (token == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }


        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getFirst();

        if (!cookieCheck.getSuccess()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        UserSession session = sessionPair.getSecond();

        List<Solution> chatSequence = solutionService.getChatSequence(caseId, session.getUserId());
        return ResponseEntity.ok(chatSequence);
    }


}

