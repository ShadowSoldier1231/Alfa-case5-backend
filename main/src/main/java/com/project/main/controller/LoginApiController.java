package com.project.main.controller;




import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.*;
import com.project.main.dto.RegisterResult;

import com.project.main.exception.*;
import com.project.main.model.*;
import com.project.main.service.FetchingService;
import com.project.main.service.SessionService;
import com.project.main.service.UserService;
import com.project.main.service.UserValidationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.validator.routines.EmailValidator;

import org.springframework.web.multipart.MultipartFile;




@RestController
@RequestMapping("/api/v1/auth")
public class LoginApiController {

    private final UserService userService;
    private final UserValidationService validationService;
    private final FetchingService fetchingService;
    private final SessionService sessionService;

    public LoginApiController(UserService userService,
                              UserValidationService validationService,
                              FetchingService fetchingService,
                              SessionService sessionService) {
        this.userService = userService;
        this.validationService = validationService;
        this.fetchingService = fetchingService;
        this.sessionService = sessionService;
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/changeemail")
    public ResponseEntity<RegisterResult> changeEmail(
            @Valid @RequestBody ChangeEmailRequest changeRequest,
            BindingResult bindingResult,
            @CookieValue(value = "token", required = false) String token) {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldError("email") != null
                    ? bindingResult.getFieldError("email").getDefaultMessage()
                    : "Invalid input data";
            throw new BadRequestException(errorMsg);
        }

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText(), token);
        }
        UserSession session = sessionPair.getRight();

        if (changeRequest.getEmail() == null || changeRequest.getEmail().isBlank()) {
            throw new BadRequestException("Email cannot be blank");
        }

        if (userService.userExistsByEmail(changeRequest.getEmail())) {
            throw new ConflictException("This email address is already taken");
        }

        if (!EmailValidator.getInstance(true).isValid(changeRequest.getEmail())) {
            throw new BadRequestException("This email address is invalid");
        }

        try {
            userService.updateEmail(session.getUserId(), changeRequest.getEmail());
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to update email");
        }

        return ResponseEntity.ok(new RegisterResult(true, "", session.getUserId()));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/changeparams")
    public ResponseEntity<RegisterResult> changeParams(
            @Valid @RequestBody ChangeParamsRequest changeRequest,
            @CookieValue(value = "token", required = false) String token) {

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText(), token);
        }
        UserSession session = sessionPair.getRight();

        if (changeRequest.getNickName() != null) {
            switch (validationService.checkUsername(changeRequest.getNickName())) {
                case TOO_LONG:
                    throw new BadRequestException("Username cannot be longer than 20 characters");
                case TOO_SHORT:
                    throw new BadRequestException("Username cannot be shorter than 3 characters");
                case EMPTY:
                    throw new BadRequestException("Username cannot be empty");
                case SPACE:
                    throw new BadRequestException("Username cannot contain spaces");
                case OK:
                    break;
            }
        }

        if (changeRequest.getCityId() != null && !fetchingService.cityExistsById(changeRequest.getCityId())) {
            throw new BadRequestException("Invalid city id");
        }

        userService.updateUserParams(session.getUserId(), changeRequest);
        return ResponseEntity.ok(new RegisterResult(true, "", session.getUserId()));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/resetpassword")
    public ResponseEntity<RegisterResult> resetPassword(
            @RequestBody ResetPasswordRequest resetPasswordRequest,
            @CookieValue(value = "token", required = false) String token,
            HttpServletResponse response) {

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText(), token);
        }
        UserSession session = sessionPair.getRight();

        if (!userService.passwordValidator(session.getUserId(), resetPasswordRequest.getOldPassword())) {
            throw new InvalidCredentialsException("Incorrect password");
        }

        switch (validationService.checkPassword(resetPasswordRequest.getNewPassword())) {
            case EMPTY:
                throw new BadRequestException("Password cannot be empty");
            case TOO_LONG:
                throw new BadRequestException("Password cannot be longer than 30 characters");
            case TOO_SHORT:
                throw new BadRequestException("Password cannot be shorter than 8 characters");
            case NO_DIGITS:
                throw new BadRequestException("Password must contain at least 1 digit");
            case NO_SPECIAL_SYMBOL:
                throw new BadRequestException("Password must contain at least 1 special character");
            case OK:
            default:
                break;
        }

        String hashedPassword = userService.hashPassword(resetPasswordRequest.getNewPassword());
        userService.updatePassword(session.getUserId(), hashedPassword);

        sessionService.deleteAllSessions(session.getUserId());
        ResponseCookie cookie = sessionService.deleteCookie(token, false);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new RegisterResult(true, "", session.getUserId()));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/setProfilePicture")
    public ResponseEntity<RegisterResult> setProfilePicture(
            @RequestParam("file") MultipartFile file,
            @CookieValue(value = "token", required = false) String token) {

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText(), token);
        }
        UserSession session = sessionPair.getRight();

        userService.saveProfilePicture(session.getUserId(), file);
        return ResponseEntity.ok(new RegisterResult(true, "", session.getUserId()));
    }

    @JsonView(Views.RegisterResultFull.class)
    @PostMapping("/register")
    public ResponseEntity<RegisterResult> registerUser(
            @Valid @RequestBody RegisterRequest registerRequest,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldError("email") != null
                    ? bindingResult.getFieldError("email").getDefaultMessage()
                    : "Invalid input data";
            throw new BadRequestException(errorMsg);
        }

        switch (validationService.checkPassword(registerRequest.getPassword())) {
            case EMPTY:
                throw new BadRequestException("Password cannot be empty");
            case TOO_LONG:
                throw new BadRequestException("Password cannot be longer than 30 characters");
            case TOO_SHORT:
                throw new BadRequestException("Password cannot be shorter than 8 characters");
            case NO_DIGITS:
                throw new BadRequestException("Password must contain at least 1 digit");
            case NO_SPECIAL_SYMBOL:
                throw new BadRequestException("Password must contain at least 1 special character");
            case OK:
            default:
                break;
        }

        switch (validationService.checkUsername(registerRequest.getUsername())) {
            case TOO_LONG:
                throw new BadRequestException("Username cannot be longer than 20 characters");
            case TOO_SHORT:
                throw new BadRequestException("Username cannot be shorter than 3 characters");
            case EMPTY:
                throw new BadRequestException("Username cannot be empty");
            case SPACE:
                throw new BadRequestException("Username cannot contain spaces");
            case OK:
            default:
                break;
        }

        if (userService.userExistsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("This email address is already taken");
        }
        if (userService.userExistsByUsername(registerRequest.getUsername())) {
            throw new ConflictException("This username is already taken");
        }
        if (!EmailValidator.getInstance(true).isValid(registerRequest.getEmail())) {
            throw new BadRequestException("This email address is invalid");
        }
        if (!fetchingService.cityExistsById(registerRequest.getCityId())) {
            throw new BadRequestException("Invalid city id");
        }

        String hashedPassword = userService.hashPassword(registerRequest.getPassword());
        Pair<String, Long> authResult = userService.registerNewUser(registerRequest, hashedPassword);

        Long userId = authResult.getRight();
        ResponseCookie preAuthCookie = sessionService.createPreAuthSession(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, preAuthCookie.toString())
                .body(new RegisterResult(true, "", authResult.getLeft(), userId));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/login")
    public ResponseEntity<RegisterResult> loginUser(
            @RequestBody LoginRequest loginRequest,
            @CookieValue(value = "token", required = false) String token,
            HttpServletResponse response) {

        if (token != null) {
            throw new BadRequestException("You are already logged in");
        }

        if (loginRequest.getUsername() == null || loginRequest.getUsername().isBlank()) {
            throw new BadRequestException("Username cannot be empty");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
            throw new BadRequestException("Password cannot be empty");
        }

        Long userId = userService.authenticateUser(loginRequest);

        ResponseCookie cookie = sessionService.generateCookie();
        sessionService.createSession(cookie.getValue(), userId);

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(new RegisterResult(true, "", userId));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @GetMapping("/logout")
    public ResponseEntity<RegisterResult> logout(
            @CookieValue(value = "token", required = false) String token,
            HttpServletResponse response) {

        if (token == null) {
            throw new InvalidSessionException("You are not logged in", token);
        }

        ResponseCookie cookie = sessionService.deleteCookie(token);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new RegisterResult(true, ""));
    }

    @JsonView(Views.MyProfile.class)
    @GetMapping("/me")
    public ResponseEntity<UserProfile> getProfile(
            @CookieValue(value = "token", required = false) String token) {

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText(), token);
        }
        Long userId = sessionPair.getRight().getUserId();

        if (userId == null || userId <= 0L) {
            throw new BadRequestException("Invalid user ID");
        }

        UserProfile profile = fetchingService.getMyProfile(userId);
        if (profile == null) {
            throw new NotFoundException("Profile not found");
        }

        return ResponseEntity.ok(profile);
    }

    @JsonView(Views.RegisterResultId.class)
    @GetMapping("/getId")
    public ResponseEntity<RegisterResult> getUserId(
            @CookieValue(value = "token", required = false) String token) {

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText(), token);
        }
        Long userId = sessionPair.getRight().getUserId();

        return ResponseEntity.ok(new RegisterResult(userId));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/verify/{code}")
    public ResponseEntity<RegisterResult> verifyUser(
            @PathVariable("code") Long verificationCode,
            @CookieValue(value = "token", required = false) String token,
            HttpServletResponse response) {

        if (token == null) {
            throw new InvalidSessionException("Verification session expired.", token);
        }

        Long tokenUserId = sessionService.getUserIdFromPreAuthCookie(token);
        if (tokenUserId == null) {
            throw new InvalidSessionException("Invalid or expired verification session.", token);
        }

        userService.verifyUser(tokenUserId, verificationCode);

        ResponseCookie clearCookie = sessionService.deleteCookie(token, false);
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        return ResponseEntity.ok(new RegisterResult(true, "", null, tokenUserId));
    }
}



