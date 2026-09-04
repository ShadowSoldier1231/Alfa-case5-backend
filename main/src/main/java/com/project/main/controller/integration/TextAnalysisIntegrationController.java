package com.project.main.controller.integration;

import com.fasterxml.jackson.annotation.JsonView;


import com.project.main.dto.cases.CasePromptResponse;
import com.project.main.dto.cases.PerfectSolutionResponse;
import com.project.main.dto.cases.RateCaseRequest;
import com.project.main.dto.common.PageResponse;
import com.project.main.dto.common.RegisterResult;
import com.project.main.dto.integration.ChatMessageDto;
import com.project.main.dto.integration.SolvingStatusResponse;
import com.project.main.dto.integration.SubmitSolutionRequest;
import com.project.main.exception.BadRequestException;
import com.project.main.model.common.Views;

import com.project.main.service.auth.SessionService;
import com.project.main.service.cases.CaseService;
import com.project.main.service.cases.SolutionService;
import com.project.main.service.user.UserModerationService;
import jakarta.servlet.http.HttpServletResponse;
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

        sessionService.checkCookieOrThrow(token);

        return ResponseEntity.ok(new RegisterResult(true, ""));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/processViolation")
    public ResponseEntity<RegisterResult> processViolation(@CookieValue(value = "token", required = false) String token,
                                                           HttpServletResponse response) {
        Long userId = sessionService.getUserIdOrThrow(token);

        String banReason = moderationService.addWarning(userId);

        if (banReason != null) {
            sessionService.deleteAllSessions(userId);
            ResponseCookie cookie = sessionService.deleteCookie(token, false);
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return ResponseEntity.ok(new RegisterResult(false, banReason, userId));
        }

        return ResponseEntity.ok(new RegisterResult(true, "", userId));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/addScore")
    public ResponseEntity<RegisterResult> addScore(@CookieValue(value = "token", required = false) String token,
                                                   @RequestBody SubmitSolutionRequest request) {
        Long userId = sessionService.getUserIdOrThrow(token);
        if (request.getRating() == null || request.getRating() < 0 || request.getRating() > 100) {
            throw new BadRequestException("Invalid rating value");
        }
        if (request.getCaseId() == null || request.getSolutionText() == null ||
                request.getSolutionResponse() == null) {
            throw new BadRequestException("Invalid request");
        }
        if (request.getSolutionText().isBlank() || request.getSolutionResponse().isBlank()) {
            throw new BadRequestException("Invalid request");
        }

        solutionService.submitSolution(userId, request);
        return ResponseEntity.ok(new RegisterResult(true, "", userId));
    }

    @PostMapping("/startSolving/{caseId}")
    public ResponseEntity<SolvingStatusResponse> startSolving(
            @CookieValue(value = "token", required = false) String token,
            @PathVariable Long caseId
    ) {
        Long userId = sessionService.getUserIdOrThrow(token);

        SolvingStatusResponse response = solutionService.startSolving(
                userId,
                caseId
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/solvingState/{caseId}")
    public ResponseEntity<SolvingStatusResponse> getSolvingState(
            @CookieValue(value = "token", required = false) String token,
            @PathVariable Long caseId) {

        Long userId = sessionService.getUserIdOrThrow(token);

        if (caseId == null || caseId <= 0) {
            throw new BadRequestException("Invalid case ID");
        }

        SolvingStatusResponse response = solutionService.getSolvingStatus(userId, caseId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/finishSolving/{caseId}")
    public ResponseEntity<SolvingStatusResponse> finishSolving(
            @CookieValue(value = "token", required = false) String token,
            @PathVariable Long caseId
    ) {
        Long userId = sessionService.getUserIdOrThrow(token);

        SolvingStatusResponse response = solutionService.finishSolving(
                userId,
                caseId
        );

        return ResponseEntity.ok(response);
    }


    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/rateCase/{caseId}")
    public ResponseEntity<RegisterResult> rateCase(
            @CookieValue(value = "token", required = false) String token,
            @PathVariable Long caseId,
            @RequestBody(required = false) RateCaseRequest request
    ) {
        Long userId = sessionService.getUserIdOrThrow(token);

        Long rating = request != null ? request.getRating() : null;

        caseService.rateCase(userId, caseId, rating);

        return ResponseEntity.ok(new RegisterResult(true, "", userId));
    }


    @GetMapping("/solutions")
    public ResponseEntity<PageResponse<ChatMessageDto>> getSolutionsForUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @CookieValue(value = "token", required = false) String token) {


        Long userId = sessionService.getUserIdOrThrow(token);
        return ResponseEntity.ok(
                solutionService.getAllSolutionsForUser(
                        userId,
                        page,
                        size
                )
        );
    }

    @GetMapping("/solutions/{caseId}")
    public ResponseEntity<PageResponse<ChatMessageDto>> getChatSequence(
            @CookieValue(value = "token", required = false) String token,
            @PathVariable Long caseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Long userId = sessionService.getUserIdOrThrow(token);

        return ResponseEntity.ok(
                solutionService.getChatSequence(
                        caseId,
                        userId,
                        page,
                        size
                )
        );
    }


    @GetMapping("/cases/{id}/perfectSolution")
    public ResponseEntity<PerfectSolutionResponse> fetchPerfectSolution(
            @PathVariable("id") Long caseId,
            @CookieValue(value = "token", required = false) String token) {

        sessionService.checkCookieOrThrow(token);
        String solution = caseService.getPerfectSolution(caseId);
        return ResponseEntity.ok(new PerfectSolutionResponse(caseId, solution));
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
