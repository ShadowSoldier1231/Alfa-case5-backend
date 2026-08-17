package com.project.main.controller;


import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.*;
import com.project.main.exception.ApiException;
import com.project.main.exception.BadRequestException;
import com.project.main.exception.InternalServerErrorException;
import com.project.main.dto.CaseAdminDto;
import com.project.main.model.Views;
import com.project.main.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.project.main.service.CaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/v1")
public class AdminApiController {

    private final CaseService caseService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(AdminApiController.class);

    public AdminApiController(CaseService caseService, UserService userService) {
        this.caseService = caseService;
        this.userService = userService;
    }

    @GetMapping("/cases")
    public ResponseEntity<PageResponse<CaseAdminDto>> getAllCasesAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "created_at,desc") String sort) {

        try {
            return ResponseEntity.ok(caseService.getAdminCases(page, size, search, sort));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while getting admin cases", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PutMapping(value = "/cases/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResult> updateCase(
            @PathVariable Long id,
            @RequestPart("case") @Valid CaseUpdateRequest req,
            BindingResult bindingResult,
            @RequestPart(value = "pdfFile", required = false) MultipartFile pdfFile,
            @RequestPart(value = "iconFile", required = false) MultipartFile iconFile
    ) {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(getValidationErrors(bindingResult));
        }

        try {
            caseService.updateCase(id, req, pdfFile, iconFile);
            return ResponseEntity.ok(new RegisterResult(true, "", null));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while updating a case", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping(value = "/createCase", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResult> createCase(
            @RequestPart("case") @Valid CaseCreateRequest request,
            BindingResult bindingResult,
            @RequestPart(value = "pdfFile", required = false) MultipartFile pdfFile,
            @RequestPart(value = "iconFile", required = false) MultipartFile iconFile
    ) {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(getValidationErrors(bindingResult));
        }

        try {
            Long caseId = caseService.createCase(request, pdfFile, iconFile);
            return ResponseEntity.ok(new RegisterResult(true, "", caseId));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while creating a case", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }
    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserListItem>> getAllUsersAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        return ResponseEntity.ok(userService.getAdminUsers(page, size, search, sort));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/users")
    public ResponseEntity<RegisterResult> createUser(
            @RequestBody @Valid AdminUserCreateRequest req,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(getValidationErrors(bindingResult));
        }

        try {
            String hashedPassword = userService.hashPassword(req.getPassword());
            Long newUserId = userService.createAdminUser(req, hashedPassword);
            return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResult(true, "", newUserId));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while creating a user", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @GetMapping("/tags")
    public ResponseEntity<PageResponse<TagListItem>> getAllTagsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        try {
            return ResponseEntity.ok(caseService.getAdminTags(page, size, search, sort));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while getting admin tags", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDetailsResponse> getUserDetails(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserDetails(id));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PatchMapping("/users/{id}")
    public ResponseEntity<RegisterResult> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid AdminUserUpdateRequest req,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(getValidationErrors(bindingResult));
        }

        try {
            userService.updateAdminUser(id, req);
            return ResponseEntity.ok(new RegisterResult(true, "", null));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while updating a user", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/tags")
    public ResponseEntity<RegisterResult> createTag(
            @RequestBody @Valid TagCreateRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(getValidationErrors(bindingResult));
        }

        try {
            Long newTagId = caseService.createTag(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResult(true, "", newTagId));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while creating a tag", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PatchMapping("/tags/{id}/deactivate")
    public ResponseEntity<RegisterResult> deactivateTag(@PathVariable Long id) {
        try {
            caseService.deactivateTag(id);
            return ResponseEntity.ok(new RegisterResult(true, "", null));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while deactivating a tag", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PatchMapping("/tags/{id}/activate")
    public ResponseEntity<RegisterResult> activateTag(@PathVariable Long id) {
        try {
            caseService.activateTag(id);
            return ResponseEntity.ok(new RegisterResult(true, "", null));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while activating a tag", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PatchMapping("/tags/{id}")
    public ResponseEntity<RegisterResult> updateTag(
            @PathVariable Long id,
            @RequestBody @Valid TagUpdateRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(getValidationErrors(bindingResult));
        }

        try {
            caseService.updateTag(id, request);
            return ResponseEntity.ok(new RegisterResult(true, "", null));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while updating a tag", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/cases/{caseId}/tags/{tagId}")
    public ResponseEntity<RegisterResult> attachTagToCase(
            @PathVariable Long caseId,
            @PathVariable Long tagId) {
        try {
            caseService.attachTagToCase(caseId, tagId);
            return ResponseEntity.ok(new RegisterResult(true, "", null));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while attaching a tag", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @DeleteMapping("/cases/{caseId}/tags/{tagId}")
    public ResponseEntity<RegisterResult> detachTagFromCase(
            @PathVariable Long caseId,
            @PathVariable Long tagId) {
        try {
            caseService.detachTagFromCase(caseId, tagId);
            return ResponseEntity.ok(new RegisterResult(true, "", null));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while detaching a tag", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @DeleteMapping("/users/{id}")
    public ResponseEntity<RegisterResult> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUserByAdmin(id);
            return ResponseEntity.ok(new RegisterResult(true, "", null));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while deleting a user", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }




    private String getValidationErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
    }

}