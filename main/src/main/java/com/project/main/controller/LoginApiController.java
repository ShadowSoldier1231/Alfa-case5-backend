package com.project.main.controller;




import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.*;
import com.project.main.dto.RegisterResult;

import com.project.main.model.*;
import com.project.main.service.FetchingService;
import com.project.main.service.SessionService;
import com.project.main.service.UserService;
import com.project.main.service.UserValidationService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.validator.routines.EmailValidator;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;


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
    public ResponseEntity<RegisterResult> changeEmail(@Valid @RequestBody ChangeEmailRequest changeRequest,
                                                      BindingResult bindingResult,
                                                      @CookieValue(value = "token", required = false) String token) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldError("email") != null
                    ? bindingResult.getFieldError("email").getDefaultMessage()
                    : "Invalid input data";
            return ResponseEntity.ok(new RegisterResult(false, errorMsg));
        }

        try {
            Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
            RegisterResult cookieCheck = sessionPair.getFirst();
            if (!cookieCheck.getSuccess()) {
                return ResponseEntity.ok(cookieCheck);
            }

            UserSession session = sessionPair.getSecond();
            if (changeRequest.getEmail() == null || changeRequest.getEmail().isBlank()) {
                return ResponseEntity.ok(new RegisterResult(false, "Email cannot be blank"));
            }


            if (userService.userExistsByEmail(changeRequest.getEmail())) {
                return ResponseEntity.ok(new RegisterResult(false, "This email address is already taken"));
            }

            if (EmailValidator.getInstance(true).isValid(changeRequest.getEmail())) {
                try {
                    userService.updateEmail(session.getUserId(), changeRequest.getEmail());
                } catch (Exception e) {
                    ResponseEntity.ok(new RegisterResult(false, "User does not exist"));
                }
                return ResponseEntity.ok(new RegisterResult(true, ""));
            } else {
                return ResponseEntity.ok(new RegisterResult(false, "This email address is invalid"));
            }
        } catch (TransactionSystemException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause instanceof ConstraintViolationException constraintEx) {
                StringBuilder errorMessage = new StringBuilder("Ошибка валидации: ");
                constraintEx.getConstraintViolations().forEach(violation -> {
                    errorMessage.append("[")
                            .append(violation.getPropertyPath())
                            .append(": ")
                            .append(violation.getMessage())
                            .append("] ");
                });
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RegisterResult(false, errorMessage.toString().trim()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RegisterResult(false, "Internal server error"));
        }
    }


    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/changeparams")
    public ResponseEntity<RegisterResult> changeParams(@Valid @RequestBody ChangeParamsRequest changeRequest,
                                                       @CookieValue(value = "token", required = false) String token) {
        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getFirst();
        if (!cookieCheck.getSuccess()) {
            return ResponseEntity.ok(cookieCheck);
        }
        UserSession session = sessionPair.getSecond();

        try {

            if (changeRequest.getNickName() != null) {
                switch (validationService.checkUsername(changeRequest.getNickName())) {
                    case TOO_LONG:
                        return ResponseEntity.ok(new RegisterResult(false, "Username cannot be longer than 20 characters"));
                    case TOO_SHORT:
                        return ResponseEntity.ok(new RegisterResult(false, "Username cannot be shorter than 3 characters"));
                    case EMPTY:
                        return ResponseEntity.ok(new RegisterResult(false, "Username cannot be empty"));
                    case SPACE:
                        return ResponseEntity.ok(new RegisterResult(false, "Username cannot contain spaces"));
                    case OK:
                        break;
                }
            }


            if (changeRequest.getCityId() != null && !fetchingService.cityExistsById(changeRequest.getCityId())) {
                return ResponseEntity.ok(new RegisterResult(false, "Invalid city id"));
            }

            userService.updateUserParams(session.getUserId(), changeRequest);
            return ResponseEntity.ok(new RegisterResult(true, ""));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new RegisterResult(false, e.getMessage()));
        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/resetpassword")
    public ResponseEntity<RegisterResult> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest,
                                                        @CookieValue(value = "token", required = false) String token,
                                                        HttpServletResponse response) {

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getFirst();
        if (!cookieCheck.getSuccess()) {
            return ResponseEntity.ok(cookieCheck);
        }
        UserSession session = sessionPair.getSecond();

        if (!userService.passwordValidator(session.getUserId(), resetPasswordRequest.getOldPassword())) {
            return ResponseEntity.ok(new RegisterResult(false, "Incorrect password"));
        }


        switch (validationService.checkPassword(resetPasswordRequest.getNewPassword())) {
            case EMPTY:
                return ResponseEntity.ok(new RegisterResult(false, "Password cannot be empty"));
            case TOO_LONG:
                return ResponseEntity.ok(new RegisterResult(false, "Password cannot be longer than 30 characters"));
            case TOO_SHORT:
                return ResponseEntity.ok(new RegisterResult(false, "Password cannot be shorter than 8 characters"));
            case NO_DIGITS:
                return ResponseEntity.ok(new RegisterResult(false, "Password must contain at least 1 digit"));
            case NO_SPECIAL_SYMBOL:
                return ResponseEntity.ok(new RegisterResult(false, "Password must contain at least 1 special character"));
            case OK:
            default:
                try {
                    userService.updatePassword(session.getUserId(), resetPasswordRequest.getNewPassword());
                } catch (Exception e) {
                    return ResponseEntity.ok(new RegisterResult(false, "User does not exist"));
                }


                sessionService.deleteAllSessions(session.getUserId());


                ResponseCookie cookie = sessionService.deleteCookie(token, false);
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                return ResponseEntity.ok(new RegisterResult(true, ""));
        }
    }


    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/setProfilePicture")
    public ResponseEntity<RegisterResult> setProfilePicture(@RequestParam("file") MultipartFile file,
                                                            @CookieValue(value = "token", required = false) String token) {

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getFirst();
        if (!cookieCheck.getSuccess()) {
            return ResponseEntity.ok(cookieCheck);
        }
        UserSession session = sessionPair.getSecond();

        if (file.isEmpty()) {
            return ResponseEntity.ok(new RegisterResult(false, "File cannot be empty"));
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/jpg"))) {
            return ResponseEntity.ok(new RegisterResult(false, "Only JPEG/JPG images are allowed"));
        }

        if (file.getSize() > 5242880) {
            return ResponseEntity.ok(new RegisterResult(false, "File size cannot exceed 5MB"));
        }

        try {

            userService.saveProfilePicture(session.getUserId(), file.getBytes());
            return ResponseEntity.ok(new RegisterResult(true, ""));
        } catch (IOException e) {
            return ResponseEntity.ok(new RegisterResult(false, "Failed to process image file"));
        }
    }



    @JsonView(Views.RegisterResultFull.class)
    @PostMapping("/register")
    public ResponseEntity<RegisterResult> registerUser(@Valid @RequestBody RegisterRequest registerRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldError("email") != null
                    ? bindingResult.getFieldError("email").getDefaultMessage()
                    : "Invalid input data";
            return ResponseEntity.ok(new RegisterResult(false, errorMsg));
        }


        switch (validationService.checkPassword(registerRequest.getPassword())) {
            case EMPTY:
                return ResponseEntity.ok(new RegisterResult(false, "Password cannot be empty"));
            case TOO_LONG:
                return ResponseEntity.ok(new RegisterResult(false, "Password cannot be longer than 30 characters"));
            case TOO_SHORT:
                return ResponseEntity.ok(new RegisterResult(false, "Password cannot be shorter than 8 characters"));
            case NO_DIGITS:
                return ResponseEntity.ok(new RegisterResult(false, "Password must contain at least 1 digit"));
            case NO_SPECIAL_SYMBOL:
                return ResponseEntity.ok(new RegisterResult(false, "Password must contain at least 1 special character"));
            case OK:
            default:
                break;
        }

        switch (validationService.checkUsername(registerRequest.getUsername())) {
            case TOO_LONG:
                return ResponseEntity.ok(new RegisterResult(false, "Username cannot be longer than 20 characters"));
            case TOO_SHORT:
                return ResponseEntity.ok(new RegisterResult(false, "Username cannot be shorter than 3 characters"));
            case EMPTY:
                return ResponseEntity.ok(new RegisterResult(false, "Username cannot be empty"));
            case SPACE:
                return ResponseEntity.ok(new RegisterResult(false, "Username cannot contain spaces"));
            case OK:
            default:
                break;
        }

        if (userService.userExistsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.ok(new RegisterResult(false, "This email address is already taken"));
        }
        if (userService.userExistsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.ok(new RegisterResult(false, "This username is already taken"));
        }
        if (!EmailValidator.getInstance(true).isValid(registerRequest.getEmail())) {
            return ResponseEntity.ok(new RegisterResult(false, "This email address is invalid"));
        }
        if (!fetchingService.cityExistsById(registerRequest.getCityId())) {
            return ResponseEntity.ok(new RegisterResult(false, "Invalid city id"));
        }

        String botUrl = userService.registerNewUser(registerRequest);

        return ResponseEntity.ok(new RegisterResult(true, "", botUrl));
    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/login")
    public ResponseEntity<RegisterResult> loginUser(@RequestBody LoginRequest loginRequest,
                                                    @CookieValue(value = "token", required = false) String token,
                                                    HttpServletResponse response) {

        if (token != null) {
            return ResponseEntity.ok(new RegisterResult(false, "You are already logged in"));
        }

        if (loginRequest.getUsername() == null || loginRequest.getUsername().isBlank()) {
            return ResponseEntity.ok(new RegisterResult(false, "Username cannot be empty"));
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
            return ResponseEntity.ok(new RegisterResult(false, "Password cannot be empty"));
        }

        try {
            Long userId = userService.authenticateUser(loginRequest);

            ResponseCookie cookie = sessionService.generateCookie();
            sessionService.createSession(cookie.getValue(), userId);

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return ResponseEntity.ok(new RegisterResult(true, ""));

        } catch (BadCredentialsException e) {
            return ResponseEntity.ok(new RegisterResult(false, e.getMessage()));
        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @GetMapping("/logout")
    public ResponseEntity<RegisterResult> logout(@CookieValue(value = "token", required = false) String token,
                                                 HttpServletResponse response) {

        if (token == null) {
            return ResponseEntity.ok(new RegisterResult(false, "You are not logged in"));
        }

        ResponseCookie cookie = sessionService.deleteCookie(token);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new RegisterResult(true, ""));
    }





}



