package com.project.main.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.CasePromptResponse;
import com.project.main.dto.RegisterResult;


import com.project.main.exception.BadRequestException;
import com.project.main.exception.InvalidSessionException;
import com.project.main.model.Solution;
import com.project.main.model.UserSession;
import com.project.main.model.Views;

import com.project.main.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;



@RestController
@RequestMapping("/api/text/v1")
public class TextAnalysisIntegrationController {

    private final UserModerationService moderationService;
    private final SessionService sessionService;
    private final CaseService caseService;
    private final SolutionService solutionService;

    public TextAnalysisIntegrationController(UserModerationService moderationService, SolutionService solutionService,
                                             CaseService caseService, SessionService sessionService) {
        this.moderationService = moderationService;
        this.solutionService = solutionService;
        this.sessionService = sessionService;
        this.caseService = caseService;
    }

    @JsonView(Views.RegisterResultPartial.class)
    @GetMapping("/checkCookie")
    public ResponseEntity<RegisterResult> checkCookie(@CookieValue(value = "token", required = false) String token) {
        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);

        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText());
        }

        return ResponseEntity.ok(sessionPair.getLeft());
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/processViolation")
    public ResponseEntity<RegisterResult> processViolation(@CookieValue(value = "token", required = false) String token,
                                                           HttpServletResponse response) {
        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText());
        }
        UserSession session = sessionPair.getRight();

        String banReason = moderationService.addWarning(session.getUserId());

        if (banReason != null) {
            sessionService.deleteAllSessions(session.getUserId());
            ResponseCookie cookie = sessionService.deleteCookie(token, false);
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok(new RegisterResult(false, banReason, session.getUserId()));
        }

        return ResponseEntity.ok(new RegisterResult(true, "", session.getUserId()));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/addScore")
    public ResponseEntity<RegisterResult> addScore(@CookieValue(value = "token", required = false) String token,
                                                   @RequestBody Solution solution) {
        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText());
        }
        UserSession session = sessionPair.getRight();

        if (solution.getCaseId() == null || solution.getSolutionText() == null ||
                solution.getSolutionResponse() == null || solution.getRating() == null) {
            throw new BadRequestException("Invalid request");
        }
        if (solution.getSolutionText().isBlank() || solution.getSolutionResponse().isBlank()) {
            throw new BadRequestException("Invalid request");
        }

        solutionService.submitSolution(session.getUserId(), solution);
        return ResponseEntity.ok(new RegisterResult(true, "", session.getUserId()));
    }

    @JsonView(Views.ChatView.class)
    @GetMapping("/getChatSequence/{caseId}")
    public ResponseEntity<List<Solution>> getChatSequence(@CookieValue(value = "token", required = false) String token,
                                                          @PathVariable Long caseId) {

        if (token == null) {
            throw new InvalidSessionException("Please login first");
        }

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText());
        }

        UserSession session = sessionPair.getRight();
        List<Solution> chatSequence = solutionService.getChatSequence(caseId, session.getUserId());

        return ResponseEntity.ok(chatSequence);
    }

    @GetMapping("/cases/{id}/prompt")
    public ResponseEntity<CasePromptResponse> getCasePrompt(
            @CookieValue(value = "token", required = false) String token,
            @PathVariable Long id) {

        sessionService.checkCookieOrThrow(token);
        CasePromptResponse prompt = caseService.getCasePrompt(id);

        return ResponseEntity.ok(prompt);
    }
}
