package com.project.main.controller.auth;




import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.common.RegisterResult;

import com.project.main.dto.auth.*;
import com.project.main.dto.user.ChangeParamsRequest;
import com.project.main.dto.user.UserProfile;
import com.project.main.exception.*;
import com.project.main.model.common.Views;
import com.project.main.model.user.UserSession;
import com.project.main.service.auth.SessionService;
import com.project.main.service.auth.VerificationRateLimitService;
import com.project.main.service.common.FetchingService;
import com.project.main.service.component.UserValidationService;
import com.project.main.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
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

import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/auth")
public class LoginApiController {

    private final UserService userService;
    private final UserValidationService validationService;
    private final FetchingService fetchingService;
    private final SessionService sessionService;
    private final VerificationRateLimitService rateLimitService;

    public LoginApiController(UserService userService,
                              UserValidationService validationService,
                              FetchingService fetchingService,
                              SessionService sessionService,
                              VerificationRateLimitService rateLimitService) {
        this.userService = userService;
        this.validationService = validationService;
        this.fetchingService = fetchingService;
        this.sessionService = sessionService;
        this.rateLimitService = rateLimitService;
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/changeemail")
    public ResponseEntity<RegisterResult> changeEmail(
            @Valid @RequestBody ChangeEmailRequest changeRequest,
            BindingResult bindingResult,
            @CookieValue(value = "token", required = false) String token) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(getValidationErrors(bindingResult));
        }

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        if (!sessionPair.getLeft().getSuccess()) {
            throw new InvalidSessionException(sessionPair.getLeft().getErrorText(), token);
        }
        UserSession session = sessionPair.getRight();

        if (changeRequest.getEmail() == null || changeRequest.getEmail().isBlank()) {
            throw new BadRequestException("Email cannot be blank");
        }

        String lowerEmail = changeRequest.getEmail().toLowerCase();
        if (userService.userExistsByEmail(lowerEmail)) {
            throw new ConflictException("This email address is already taken");
        }
        if (!EmailValidator.getInstance(true).isValid(lowerEmail)) {
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
            @CookieValue(value = "token", required = false) String token,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(getValidationErrors(bindingResult));
        }

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
            BindingResult bindingResult,
            HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(getValidationErrors(bindingResult));
        }

        String clientIp = request.getRemoteAddr();
        rateLimitService.checkCanSendEmail(clientIp);

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

        rateLimitService.recordEmailSent(clientIp, userId);

        ResponseCookie preAuthCookie = sessionService.createPreAuthSession(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, preAuthCookie.toString())
                .body(new RegisterResult(true, "", authResult.getLeft(), userId));
    }


    @JsonView(Views.RegisterResultFull.class)
    @PostMapping("/resendEmail")
    public ResponseEntity<RegisterResult> resendEmail(
            @Valid @RequestBody ResendEmailRequest resendEmailRequest,
            BindingResult bindingResult,
            HttpServletRequest request,
            @CookieValue(value = "token", required = false) String token) {

        sessionService.reverseCheckCookieOrThrow(token);

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(getValidationErrors(bindingResult));
        }

        String clientIp = request.getRemoteAddr();
        rateLimitService.checkCanSendEmail(clientIp);

        switch (validationService.checkPassword(resendEmailRequest.getPassword())) {
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

        switch (validationService.checkUsername(resendEmailRequest.getUsername())) {
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

        if (!EmailValidator.getInstance(true).isValid(resendEmailRequest.getEmail())) {
            throw new BadRequestException("This email address is invalid");
        }

        Pair<String, Long> result = userService.resendEmailOrThrow(resendEmailRequest);

        Long userId = result.getRight();
        rateLimitService.recordEmailSent(clientIp, userId);

        ResponseCookie preAuthCookie = sessionService.createPreAuthSession(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, preAuthCookie.toString())
                .body(new RegisterResult(true, "", result.getLeft(), userId));
    }


    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/forgotUsername")
    public ResponseEntity<RegisterResult> forgotUsername(
            @Valid @RequestBody ForgotUsernameRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(getValidationErrors(bindingResult));
        }

        String clientIp = httpRequest.getRemoteAddr();
        rateLimitService.checkCanSendEmail(clientIp);

        userService.processForgotUsername(request.getEmail(), clientIp);

        return ResponseEntity.ok(new RegisterResult(true, ""));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/forgotPassword/init")
    public ResponseEntity<RegisterResult> forgotPasswordInit(
            @Valid @RequestBody ForgotPasswordInitRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(getValidationErrors(bindingResult));
        }

        String clientIp = httpRequest.getRemoteAddr();
        rateLimitService.checkCanSendEmail(clientIp);

        userService.initiatePasswordReset(request.getEmail(), request.getUsername(), clientIp);

        return ResponseEntity.ok(new RegisterResult(true, ""));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/forgotPassword/confirm")
    public ResponseEntity<RegisterResult> forgotPasswordConfirm(
            @Valid @RequestBody ForgotPasswordConfirmRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(getValidationErrors(bindingResult));
        }

        String clientIp = httpRequest.getRemoteAddr();
        rateLimitService.checkCanAttemptPasswordReset(clientIp);

        switch (validationService.checkPassword(request.getNewPassword())) {
            case EMPTY: throw new BadRequestException("Password cannot be empty");
            case TOO_LONG: throw new BadRequestException("Password cannot be longer than 30 characters");
            case TOO_SHORT: throw new BadRequestException("Password cannot be shorter than 8 characters");
            case NO_DIGITS: throw new BadRequestException("Password must contain at least 1 digit");
            case NO_SPECIAL_SYMBOL: throw new BadRequestException("Password must contain at least 1 special character");
            case OK: default: break;
        }

        String hashedPassword = userService.hashPassword(request.getNewPassword());

        try {
            Long userId = userService.confirmPasswordReset(
                    request.getEmail(),
                    request.getUsername(),
                    request.getCode(),
                    hashedPassword
            );

            sessionService.deleteAllSessions(userId);
            rateLimitService.clearPasswordResetAttemptsOnSuccess(clientIp);

            return ResponseEntity.ok(new RegisterResult(true, "", userId));

        } catch (BadRequestException e) {
            rateLimitService.recordFailedPasswordResetAttempt(clientIp);
            throw e;
        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/login")
    public ResponseEntity<RegisterResult> loginUser(
            @RequestBody LoginRequest loginRequest,
            @CookieValue(value = "token", required = false) String token,
            HttpServletResponse response) {

        sessionService.reverseCheckCookieOrThrow(token);

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
            HttpServletRequest request,
            HttpServletResponse response) {

        if (token == null) {
            throw new InvalidSessionException("Verification session expired.", token);
        }

        Long tokenUserId = sessionService.getUserIdFromPreAuthCookie(token);
        if (tokenUserId == null) {
            throw new InvalidSessionException("Invalid or expired verification session.", token);
        }

        String clientIp = request.getRemoteAddr();
        rateLimitService.checkCanVerifyCode(clientIp, tokenUserId);

        try {
            userService.verifyUser(tokenUserId, verificationCode);

            rateLimitService.clearVerifyAttemptsOnSuccess(tokenUserId);

            ResponseCookie clearCookie = sessionService.deleteCookie(token, false);
            response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
            return ResponseEntity.ok(new RegisterResult(true, "", null, tokenUserId));
        } catch (BadRequestException e) {
            rateLimitService.recordFailedVerifyAttempt(clientIp, tokenUserId);
            throw e;
        }
    }

    private String getValidationErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
    }
}



