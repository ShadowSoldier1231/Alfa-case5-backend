package com.project.main.controller.auth;




import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.common.RegisterResult;

import com.project.main.dto.auth.*;
import com.project.main.dto.user.ChangeParamsRequest;
import com.project.main.dto.user.UserProfile;
import com.project.main.exception.*;
import com.project.main.model.common.Views;
import com.project.main.service.auth.SessionService;
import com.project.main.service.auth.VerificationRateLimitService;
import com.project.main.service.common.FetchingService;
import com.project.main.service.component.ControllerHelperService;
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



@RestController
@RequestMapping("/api/v1/auth")
public class LoginApiController {

    private final UserService userService;
    private final FetchingService fetchingService;
    private final SessionService sessionService;
    private final VerificationRateLimitService rateLimitService;
    private final ControllerHelperService controllerHelper;

    public LoginApiController(UserService userService,
                              FetchingService fetchingService,
                              SessionService sessionService,
                              VerificationRateLimitService rateLimitService,
                              ControllerHelperService controllerHelper) {
        this.userService = userService;
        this.fetchingService = fetchingService;
        this.sessionService = sessionService;
        this.rateLimitService = rateLimitService;
        this.controllerHelper = controllerHelper;
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/changeEmail")
    public ResponseEntity<RegisterResult> changeEmail(
            @Valid @RequestBody ChangeEmailRequest changeRequest,
            BindingResult bindingResult,
            @CookieValue(value = "token", required = false) String token) {

        controllerHelper.validateBindingResult(bindingResult);

        Long userId = sessionService.getUserIdOrThrow(token);

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
            userService.updateEmail(userId, changeRequest.getEmail());
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to update email");
        }

        return ResponseEntity.ok(new RegisterResult(true, "", userId));
    }

    @PostMapping("/changeParams")
    public ResponseEntity<RegisterResult> changeParams(
            @Valid @RequestBody ChangeParamsRequest changeRequest,
            BindingResult bindingResult,
            @CookieValue(value = "token", required = false) String token) {

        controllerHelper.validateBindingResult(bindingResult);

        Long userId = sessionService.getUserIdOrThrow(token);

        if (changeRequest.getNickName() != null) {
            controllerHelper.validateUsername(changeRequest.getNickName());
        }

        if (changeRequest.getCityId() != null && !fetchingService.cityExistsById(changeRequest.getCityId())) {
            throw new BadRequestException("Invalid city id");
        }

        userService.updateUserParams(userId, changeRequest);
        return ResponseEntity.ok(new RegisterResult(true, "", userId));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/resetPassword")
    public ResponseEntity<RegisterResult> resetPassword(
            @RequestBody ResetPasswordRequest resetPasswordRequest,
            @CookieValue(value = "token", required = false) String token,
            HttpServletResponse response) {

        Long userId = sessionService.getUserIdOrThrow(token);

        if (!userService.passwordValidator(userId, resetPasswordRequest.getOldPassword())) {
            throw new InvalidCredentialsException("Incorrect password");
        }

        controllerHelper.validatePassword(resetPasswordRequest.getNewPassword());

        String hashedPassword = userService.hashPassword(resetPasswordRequest.getNewPassword());
        userService.updatePassword(userId, hashedPassword);
        sessionService.deleteAllSessions(userId);

        ResponseCookie cookie = sessionService.deleteCookie(token, false);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new RegisterResult(true, "", userId));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/setProfilePicture")
    public ResponseEntity<RegisterResult> setProfilePicture(
            @RequestParam("file") MultipartFile file,
            @CookieValue(value = "token", required = false) String token) {

        Long userId = sessionService.getUserIdOrThrow(token);

        userService.saveProfilePicture(userId, file);
        return ResponseEntity.ok(new RegisterResult(true, "", userId));
    }

    @JsonView(Views.RegisterResultFull.class)
    @PostMapping("/register")
    public ResponseEntity<RegisterResult> registerUser(
            @Valid @RequestBody RegisterRequest registerRequest,
            BindingResult bindingResult,
            HttpServletRequest request) {

        controllerHelper.validateBindingResult(bindingResult);

        String clientIp = request.getRemoteAddr();
        rateLimitService.checkCanSendEmail(clientIp);

        controllerHelper.validatePassword(registerRequest.getPassword());
        controllerHelper.validateUsername(registerRequest.getUsername());

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

        controllerHelper.validateBindingResult(bindingResult);

        String clientIp = request.getRemoteAddr();
        rateLimitService.checkCanSendEmail(clientIp);

        controllerHelper.validatePassword(resendEmailRequest.getPassword());
        controllerHelper.validateUsername(resendEmailRequest.getUsername());

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

        controllerHelper.validateBindingResult(bindingResult);

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

        controllerHelper.validateBindingResult(bindingResult);

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

        controllerHelper.validateBindingResult(bindingResult);

        String clientIp = httpRequest.getRemoteAddr();
        rateLimitService.checkCanAttemptPasswordReset(clientIp);

        controllerHelper.validatePassword(request.getNewPassword());

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

        Long userId = sessionService.getUserIdOrThrow(token);

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

        Long userId = sessionService.getUserIdOrThrow(token);

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

}



