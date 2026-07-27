package com.project.main.controller;


import com.project.main.dto.RegisterResult;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.main.dto.CaseCreateRequest;

import com.project.main.model.UserSession;
import com.project.main.service.CaseService;
import com.project.main.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/v1")
public class AdminApiController {

    private final SessionService sessionService;
    private final CaseService caseService;

    public AdminApiController(SessionService sessionService,
                              CaseService caseService) {
        this.sessionService = sessionService;
        this.caseService = caseService;
    }

    @PostMapping(value = "/createCase", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResult> createCase(
            @CookieValue(value = "token", required = false) String token,
            @RequestPart("case") CaseCreateRequest request,
            @RequestPart(value = "pdfFile", required = false) MultipartFile pdfFile,
            @RequestPart(value = "iconFile", required = false) MultipartFile iconFile
    ) {

        Pair<RegisterResult, UserSession> authResult = sessionService.checkCookie(token);

        if (!authResult.getLeft().getSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResult.getLeft());
        }

        try {
            caseService.createCase(request, pdfFile, iconFile);

            RegisterResult successResult = new RegisterResult();
            successResult.setSuccess(true);
            successResult.setErrorText("");
            successResult.setId(authResult.getRight().getUserId());

            return ResponseEntity.ok(successResult);

        } catch (IllegalArgumentException e) {
            RegisterResult errorResult = new RegisterResult();
            errorResult.setSuccess(false);
            errorResult.setErrorText(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);

        } catch (Exception e) {
            RegisterResult errorResult = new RegisterResult();
            errorResult.setSuccess(false);
            errorResult.setErrorText("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }
}
