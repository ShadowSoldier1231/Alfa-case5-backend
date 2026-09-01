package com.project.main.controller.admin;


import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.cases.*;
import com.project.main.dto.common.PageResponse;
import com.project.main.dto.common.RegisterResult;
import com.project.main.dto.integration.ChatMessageDto;
import com.project.main.dto.learing.*;
import com.project.main.dto.tags.TagCreateRequest;
import com.project.main.dto.tags.TagListItem;
import com.project.main.dto.tags.TagUpdateRequest;
import com.project.main.dto.user.AdminUserCreateRequest;
import com.project.main.dto.user.AdminUserUpdateRequest;
import com.project.main.dto.user.UserDetailsResponse;
import com.project.main.dto.user.UserListItem;
import com.project.main.exception.ApiException;
import com.project.main.exception.InternalServerErrorException;
import com.project.main.model.common.Views;
import com.project.main.service.cases.SolutionService;
import com.project.main.service.component.ControllerHelperService;
import com.project.main.service.learning.QuizService;
import com.project.main.service.user.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.project.main.service.cases.CaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/v1")
public class AdminApiController {

    private final CaseService caseService;
    private final UserService userService;
    private final SolutionService solutionService;
    private final QuizService quizService;
    private final ControllerHelperService controllerHelper;
    private static final Logger logger = LoggerFactory.getLogger(AdminApiController.class);

    public AdminApiController(CaseService caseService, UserService userService,
                              SolutionService solutionService,
                              QuizService quizService,
                              ControllerHelperService controllerHelper) {
        this.caseService = caseService;
        this.userService = userService;
        this.solutionService = solutionService;
        this.quizService = quizService;
        this.controllerHelper = controllerHelper;
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
        controllerHelper.validateBindingResult(bindingResult);

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

    @GetMapping("/cases/{caseId}/solutions")
    public ResponseEntity<PageResponse<ChatMessageDto>> getSolutionsForCase(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @PathVariable("caseId") Long caseId) {

        return ResponseEntity.ok(
                solutionService.getAllSolutionsForCase(
                        caseId,
                        page,
                        size
                )
        );
    }

    @GetMapping("/cases/{caseId}/theory")
    public ResponseEntity<AdminMaterialDto> getCaseTheory(
            @PathVariable("caseId") Long caseId
    ) {
        try {
            return ResponseEntity.ok(caseService.getAdminMaterials(caseId));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while getting admin case theory", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @GetMapping("/theory/{id}")
    public ResponseEntity<AdminPartialMaterialDto> getTheoryMaterial(
            @PathVariable("id") Long id
    ) {
        try {
            return ResponseEntity.ok(caseService.getAdminMaterialById(id));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while getting admin theory material", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @GetMapping("/theory/{id}/quiz")
    public ResponseEntity<AdminTheoryQuizResponse> getAdminQuizByTheoryId(
            @PathVariable("id") Long id
    ) {
        try {
            return ResponseEntity.ok(quizService.getAdminQuizByMaterialId(id));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while getting admin quiz by theory id", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PutMapping("/theory/{id}/quiz")
    public ResponseEntity<RegisterResult> upsertQuiz(
            @PathVariable("id") Long materialId,
            @RequestBody @Valid QuizUpsertRequest request,
            BindingResult bindingResult
    ) {
        controllerHelper.validateBindingResult(bindingResult);
        try {
            Long quizId= quizService.upsertQuiz(materialId, request);
            return ResponseEntity.ok(new RegisterResult(true, "", quizId));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while upserting quiz", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @JsonView(Views.RegisterResultId.class)
    @PostMapping("/cases/{caseId}/theory")
    public ResponseEntity<RegisterResult> createTheoryMaterial(
            @PathVariable("caseId") Long caseId,
            @RequestBody @Valid TheoryCreateRequest request,
            BindingResult bindingResult
    ) {
        controllerHelper.validateBindingResult(bindingResult);

        try {
            Long materialId = caseService.createTheoryMaterial(caseId, request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new RegisterResult(true, "", materialId));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while creating theory material", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PatchMapping("/theory/{id}")
    public ResponseEntity<RegisterResult> updateTheoryMaterial(
            @PathVariable("id") Long id,
            @RequestBody @Valid TheoryUpdateRequest request,
            BindingResult bindingResult
    ) {
        controllerHelper.validateBindingResult(bindingResult);

        try {
            caseService.updateTheoryMaterial(id, request);
            return ResponseEntity.ok(new RegisterResult(true, "", null));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while updating theory material", e);
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
        controllerHelper.validateBindingResult(bindingResult);

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

        try {
            return ResponseEntity.ok(userService.getAdminUsers(page, size, search, sort));
        }catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while fetching users", e);
            throw new InternalServerErrorException("Internal server error");
        }

    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/users")
    public ResponseEntity<RegisterResult> createUser(
            @RequestBody @Valid AdminUserCreateRequest req,
            BindingResult bindingResult
    ) {
        controllerHelper.validateBindingResult(bindingResult);

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
        try {
            return ResponseEntity.ok(userService.getUserDetails(id));
        }catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Internal server error while fetching a user", e);
            throw new InternalServerErrorException("Internal server error");
        }
    }

    @GetMapping("/users/{userId}/solutions/case/{caseId}")
    public ResponseEntity<PageResponse<ChatMessageDto>> getSolutionsForCase(
            @PathVariable("caseId") Long caseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @PathVariable("userId") Long userId) {

        return ResponseEntity.ok(
                solutionService.getChatSequence(
                        caseId,
                        userId,
                        page,
                        size
                )
        );
    }

    @GetMapping("/users/{userId}/solutions")
    public ResponseEntity<PageResponse<ChatMessageDto>> getSolutionsForUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @PathVariable("userId") Long userId) {

        return ResponseEntity.ok(
                solutionService.getAllSolutionsForUser(
                        userId,
                        page,
                        size
                )
        );
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PatchMapping("/users/{id}")
    public ResponseEntity<RegisterResult> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid AdminUserUpdateRequest req,
            BindingResult bindingResult
    ) {
        controllerHelper.validateBindingResult(bindingResult);

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
        controllerHelper.validateBindingResult(bindingResult);

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
        controllerHelper.validateBindingResult(bindingResult);

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


}