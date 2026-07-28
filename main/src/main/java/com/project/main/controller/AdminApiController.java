package com.project.main.controller;


import com.project.main.dto.*;
import com.project.main.model.CaseEntity;
import com.project.main.service.UserService;
import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.project.main.model.UserSession;
import com.project.main.service.CaseService;
import com.project.main.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/admin/v1")
public class AdminApiController {

    private final SessionService sessionService;
    private final CaseService caseService;
    private final UserService userService;

    public AdminApiController(SessionService sessionService, CaseService caseService, UserService userService) {
        this.sessionService = sessionService;
        this.caseService = caseService;
        this.userService = userService;
    }




    @GetMapping("/cases")
    public ResponseEntity<List<CaseEntity>> getAllCasesAdmin() {
        return ResponseEntity.ok(caseService.getAllAdminCases());
    }

    @PutMapping(value = "/cases/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResult> updateCase(
            @PathVariable Long id,
            @RequestPart("case") @Valid CaseUpdateRequest req,
            BindingResult bindingResult,
            @RequestPart(value = "pdfFile", required = false) MultipartFile pdfFile,
            @RequestPart(value = "iconFile", required = false) MultipartFile iconFile
    ) {
        if (bindingResult.hasErrors()) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText(getValidationErrors(bindingResult));
            return ResponseEntity.badRequest().body(res);
        }

        try {
            caseService.updateCase(id, req, pdfFile, iconFile);
            RegisterResult res = new RegisterResult();
            res.setSuccess(true);
            res.setErrorText("");
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText(e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
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
            System.err.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }



    @PostMapping("/users")
    public ResponseEntity<RegisterResult> createUser(
            @RequestBody @Valid AdminUserCreateRequest req,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText(getValidationErrors(bindingResult));
            return ResponseEntity.badRequest().body(res);
        }

        try {
            String hashedPassword = userService.hashPassword(req.getPassword());
            Long newUserId = userService.createAdminUser(req, hashedPassword);
            RegisterResult res = new RegisterResult();
            res.setSuccess(true);
            res.setErrorText("");
            res.setId(newUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (IllegalArgumentException e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText(e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<RegisterResult> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid AdminUserUpdateRequest req,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText(getValidationErrors(bindingResult));
            return ResponseEntity.badRequest().body(res);
        }

        try {
            userService.updateAdminUser(id, req);
            RegisterResult res = new RegisterResult();
            res.setSuccess(true);
            res.setErrorText("");
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText(e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }


    @PostMapping("/tags")
    public ResponseEntity<RegisterResult> createTag(
            @RequestBody @Valid TagCreateRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText(getValidationErrors(bindingResult));
            return ResponseEntity.badRequest().body(res);
        }

        try {
            Long newTagId = caseService.createTag(request);
            RegisterResult res = new RegisterResult();
            res.setSuccess(true);
            res.setErrorText("");
            res.setId(newTagId);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (IllegalArgumentException e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText(e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            System.err.println(e.getMessage());
            res.setErrorText("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }
    @PatchMapping("/tags/{id}/deactivate")
    public ResponseEntity<RegisterResult> deactivateTag(@PathVariable Long id) {
        try {
            caseService.deactivateTag(id);
            RegisterResult res = new RegisterResult();
            res.setSuccess(true);
            res.setErrorText("");
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText(e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText("Internal server error");
            System.err.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }
    @PostMapping("/cases/{caseId}/tags/{tagId}")
    public ResponseEntity<RegisterResult> attachTagToCase(
            @PathVariable Long caseId,
            @PathVariable Long tagId) {
        try {
            caseService.attachTagToCase(caseId, tagId);
            RegisterResult res = new RegisterResult();
            res.setSuccess(true);
            res.setErrorText("");
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText(e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText("Internal server error");
            System.err.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    @DeleteMapping("/cases/{caseId}/tags/{tagId}")
    public ResponseEntity<RegisterResult> detachTagFromCase(
            @PathVariable Long caseId,
            @PathVariable Long tagId) {
        try {
            caseService.detachTagFromCase(caseId, tagId);
            RegisterResult res = new RegisterResult();
            res.setSuccess(true);
            res.setErrorText("");
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText(e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText("Internal server error");
            System.err.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }


    @DeleteMapping("/users/{id}")
    public ResponseEntity<RegisterResult> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUserByAdmin(id);
            RegisterResult res = new RegisterResult();
            res.setSuccess(true);
            res.setErrorText("");
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText(e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            RegisterResult res = new RegisterResult();
            res.setSuccess(false);
            res.setErrorText("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }







    private String getValidationErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
    }

}
