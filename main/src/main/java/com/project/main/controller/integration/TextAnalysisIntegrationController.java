package com.project.main.controller.integration;

import com.fasterxml.jackson.annotation.JsonView;


import com.project.main.dto.cases.CasePromptResponse;
import com.project.main.dto.common.PageResponse;
import com.project.main.dto.common.RegisterResult;
import com.project.main.dto.integration.ChatMessageDto;
import com.project.main.dto.integration.SubmitSolutionRequest;
import com.project.main.exception.BadRequestException;
import com.project.main.exception.InvalidSessionException;
import com.project.main.model.user.UserSession;
import com.project.main.model.common.Views;

import com.project.main.service.auth.SessionService;
import com.project.main.service.cases.CaseService;
import com.project.main.service.cases.SolutionService;
import com.project.main.service.user.UserModerationService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



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
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText(), token);
        }

        return ResponseEntity.ok(sessionPair.getLeft());
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/processViolation")
    public ResponseEntity<RegisterResult> processViolation(@CookieValue(value = "token", required = false) String token,
                                                           HttpServletResponse response) {
        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText(), token);
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
                                                   @RequestBody SubmitSolutionRequest request) {
        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText(), token);
        }
        UserSession session = sessionPair.getRight();

        if (request.getCaseId() == null || request.getSolutionText() == null ||
                request.getSolutionResponse() == null || request.getRating() == null) {
            throw new BadRequestException("Invalid request");
        }
        if (request.getSolutionText().isBlank() || request.getSolutionResponse().isBlank()) {
            throw new BadRequestException("Invalid request");
        }

        solutionService.submitSolution(session.getUserId(), request);
        return ResponseEntity.ok(new RegisterResult(true, "", session.getUserId()));
    }

    @GetMapping("/getChatSequence/{caseId}")
    public ResponseEntity<PageResponse<ChatMessageDto>> getChatSequence(
            @CookieValue(value = "token", required = false) String token,
            @PathVariable Long caseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        if (token == null) {
            throw new InvalidSessionException("Please login first", token);
        }

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);

        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText(), token);
        }

        UserSession session = sessionPair.getRight();

        return ResponseEntity.ok(
                solutionService.getChatSequence(
                        caseId,
                        session.getUserId(),
                        page,
                        size
                )
        );
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
