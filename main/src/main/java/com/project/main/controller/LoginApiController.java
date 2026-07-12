package com.project.main.controller;




import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.*;
import com.project.main.dto.RegisterResult;
import com.project.main.enums.GenderCode;
import com.project.main.enums.UserRole;
import com.project.main.enums.UserStatus;
import com.project.main.enums.UserStatus.*;
import com.project.main.model.*;
import com.project.main.repository.*;
import com.project.main.service.FetchingService;
import com.project.main.service.SessionService;
import com.project.main.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.validator.routines.EmailValidator;
import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import static com.project.main.enums.UserStatus.*;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginApiController {




    private final UserService userService;
    private final FetchingService fetchingService;
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;
    private final UserSessionRepository sessionRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final  UserAvatarRepository userAvatarRepository;

    public LoginApiController(UserRepository userRepository, UserService userService, UserSessionRepository userSessionRepository,
                              UserDataRepository userDataRepository, SessionService sessionService,
                              LeaderboardRepository leaderboardRepository, UserAvatarRepository userAvatarRepository,
                              FetchingService fetchingService) {
        this.userDataRepository = userDataRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.sessionRepository = userSessionRepository;
        this.sessionService = sessionService;
        this.leaderboardRepository = leaderboardRepository;
        this.userAvatarRepository = userAvatarRepository;
        this.fetchingService = fetchingService;

    }



    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/changeemail")
    public ResponseEntity<RegisterResult> changeEmail(@Valid @RequestBody ChangeRequest changeRequest, BindingResult bindingResult,
                                                      @CookieValue(value = "token", required = false) String token){
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
            if (changeRequest.getEmail() == null) {
                return ResponseEntity.ok(new RegisterResult(false, "Email cannot be blank"));
            }
            if (changeRequest.getEmail().isBlank()) {
                return ResponseEntity.ok(new RegisterResult(false, "Email cannot be blank"));
            }
            if (userRepository.existsByEmail(changeRequest.getEmail()))
                return ResponseEntity.ok(new RegisterResult(false, "This email address is already taken"));
            if (EmailValidator.getInstance(true).isValid(changeRequest.getEmail())) {
                userService.updateEmail(session.getUserId(), changeRequest.getEmail());
                return ResponseEntity.ok(new RegisterResult(true, ""));
            } else {
                return ResponseEntity.ok(new RegisterResult(false, "This email address is invalid"));
            }
        }  catch (TransactionSystemException e) {
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


    @Transactional
    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/changeparams")
    public ResponseEntity<RegisterResult> changeParams(@Valid @RequestBody ChangeRequest changeRequest, @CookieValue(value = "token", required = false) String token){
        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getFirst();
        if(!cookieCheck.getSuccess()){
            return ResponseEntity.ok(cookieCheck);
        }
        UserSession session = sessionPair.getSecond();

        UserData realUser = userDataRepository.findById(session.getUserId())
                .orElse(null);
        if (realUser == null){
            return ResponseEntity.ok(new RegisterResult(false, "User does not exist"));
        }

        if(changeRequest.getCityId() != null){

            if(fetchingService.cityExistsById(changeRequest.getCityId())) {
                realUser.setCityId(changeRequest.getCityId());
            }else {
                return ResponseEntity.ok(new RegisterResult(false, "Invalid city id"));
            }
        }
        if(changeRequest.getBirthdate() != null){
            realUser.setBirthdate(changeRequest.getBirthdate());
        }
        if(changeRequest.getStatus() != null){
            switch (changeRequest.getStatus()){
                case WORKER:
                    realUser.setStatus(WORKER);
                    break;
                case STUDENT5:
                    realUser.setStatus(UserStatus.STUDENT5);
                    break;
                case STUDENT10:
                    realUser.setStatus(UserStatus.STUDENT10);
                    break;
                case POSTGRADUATE:
                    realUser.setStatus(UserStatus.POSTGRADUATE);
                    break;
                case UNDERGRADUATE:
                    realUser.setStatus(UserStatus.UNDERGRADUATE);
                    break;
                case OTHER:
                    realUser.setStatus(UserStatus.OTHER);
                    break;
                case COLLEGE_STUDENT:
                    realUser.setStatus(UserStatus.COLLEGE_STUDENT);
                    break;
                default:
                    return ResponseEntity.ok(new RegisterResult(false, "Invalid user status code"));
            }
        }
        if (changeRequest.getFirstName() != null){
            if (!changeRequest.getFirstName().isEmpty()) {
                realUser.setFirstName(changeRequest.getFirstName());
            }
        }
        if(changeRequest.getLastName() != null){
            if (!changeRequest.getLastName().isEmpty()){
                realUser.setLastName(changeRequest.getLastName());
            }
        }

        userDataRepository.save(realUser);
        return ResponseEntity.ok(new RegisterResult(true, ""));

    }


    @JsonView(Views.RegisterResultPartial.class)
    @Transactional
    @PostMapping("/resetpassword")
    public ResponseEntity<RegisterResult> resetPassword( @RequestBody ChangeRequest changeRequest, @CookieValue(value = "token", required = false) String token, HttpServletResponse response){

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getFirst();
        if(!cookieCheck.getSuccess()){
            return ResponseEntity.ok(cookieCheck);
        }
        UserSession session = sessionPair.getSecond();

        if(userService.passwordValidator(session.getUserId(), changeRequest.getOldPassword() )) {

            switch (userService.checkPassword(changeRequest.getNewPassword())){

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
                default:

                    userService.updatePassword(session.getUserId(), changeRequest.getNewPassword());
                    sessionRepository.deleteByUserId(session.getUserId());

                    ResponseCookie cookie = sessionService.deleteCookie(token, false);

                    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                    return ResponseEntity.ok(new RegisterResult(true, ""));
            }



        } else {
            return ResponseEntity.ok(new RegisterResult(false, "Incorrect password"));
        }

    }


    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/setProfilePicture")
    @Transactional
    public ResponseEntity<RegisterResult> setProfilePicture(
            @RequestParam("file") MultipartFile file,
            @CookieValue(value = "token", required = false) String token) {

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getFirst();
        if(!cookieCheck.getSuccess()){
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

            UserAvatar avatar = new UserAvatar(session.getUserId(), file.getBytes());
            userAvatarRepository.save(avatar);

            return ResponseEntity.ok(new RegisterResult(true, ""));

        } catch (IOException e) {
            return ResponseEntity.ok(new RegisterResult(false, "Failed to process image file"));
        }
    }



    @JsonView(Views.RegisterResultFull.class)
    @PostMapping("/register")
    public ResponseEntity<RegisterResult> registerUser(@RequestBody RegisterRequest registerRequest, BindingResult bindingResult){
        String botUrl = "";

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldError("email") != null
                    ? bindingResult.getFieldError("email").getDefaultMessage()
                    : "Invalid input data";
            return ResponseEntity.ok(new RegisterResult(false, errorMsg));
        }

        switch (userService.checkPassword(registerRequest.getPassword())){

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
            default:
                break;
        }
        switch (userService.checkUsername(registerRequest.getUsername())){
            case TOO_LONG:
                return ResponseEntity.ok(new RegisterResult(false, "Username cannot be longer than 20 characters"));
            case TOO_SHORT:
                return ResponseEntity.ok(new RegisterResult(false, "Username cannot be shorter than 3 characters"));
            case EMPTY:
                return ResponseEntity.ok(new RegisterResult(false, "Username cannot be empty"));
            case SPACE:
                return  ResponseEntity.ok(new RegisterResult(false, "Username cannot contain spaces"));
            case OK: {

                if (userRepository.existsByEmail(registerRequest.getEmail())){
                    return  ResponseEntity.ok(new RegisterResult(false, "This email address is already taken"));
                }else if (userRepository.existsByUsername(registerRequest.getUsername())){
                    return  ResponseEntity.ok(new RegisterResult(false, "This username is already taken"));
                }else {
                    if(! EmailValidator.getInstance(true).isValid(registerRequest.getEmail())) {
                        return  ResponseEntity.ok(new RegisterResult(false, "This email address is invalid"));
                    }

                    if(!fetchingService.cityExistsById(registerRequest.getCityId())) {
                        return ResponseEntity.ok(new RegisterResult(false, "Invalid city id"));
                    }


                    UserSetup validUser = new UserSetup(registerRequest.getPassword(),
                            registerRequest.getUsername(), registerRequest.getEmail(), UserRole.USER, null);
                    botUrl = userService.save(validUser);



                    userDataRepository.save(new UserData(validUser.getId(), registerRequest.getFirstName(), registerRequest.getLastName(),
                            registerRequest.getBirthdate(), registerRequest.getStatus(), registerRequest.getCityId(), registerRequest.getMiddleName(),
                                (registerRequest.getGender() != null) ? registerRequest.getGender() : GenderCode.NOT_STATED)
                        );
                    leaderboardRepository.save( new LeaderboardUser(validUser.getId(), 0L,
                                0L, 0L, 0L)
                        );

                }
            }
        }
        return ResponseEntity.ok( new RegisterResult(true, "", botUrl));

    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/login")
    public ResponseEntity<RegisterResult> loginUser(@RequestBody LoginRequest loginRequest,
                                    @CookieValue(value = "token", required = false) String token, HttpServletResponse response){

        if (token != null) {
            return ResponseEntity.ok(new RegisterResult(false, "You are already logged in"));
        }

        if(loginRequest.getPassword() == null){
            return ResponseEntity.ok(new RegisterResult(false, "Password cannot be empty"));
        }
        if(loginRequest.getUsername() == null){
            return ResponseEntity.ok(new RegisterResult(false, "Username cannot be empty"));
        }


        if(loginRequest.getPassword().isBlank()){
            return ResponseEntity.ok(new RegisterResult(false, "Password cannot be empty"));
        }else if(loginRequest.getUsername().isBlank()){
            return ResponseEntity.ok(new RegisterResult(false, "Username cannot be empty"));
        }
        else{

            UserSetup realUser = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);

            if ( realUser != null ) {
                if (userService.loginUser(loginRequest, realUser)) {


                    if(realUser.getBannedUntil() != null){
                        if(realUser.getBannedUntil().isAfter(LocalDateTime.now())){
                            return ResponseEntity.ok(new RegisterResult(false, "User is still banned"));
                        }
                        realUser.setBannedUntil(null);
                        userRepository.save(realUser);
                    }

                    ResponseCookie cookie = sessionService.generateCookie();
                    sessionService.createSession(cookie.getValue(), realUser.getId());

                    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                    return ResponseEntity.ok(new RegisterResult(true, ""));
                }
            }

            return ResponseEntity.ok(new RegisterResult(false, "Invalid username or password"));

        }
    }

    @JsonView(Views.RegisterResultPartial.class)
    @Transactional
    @GetMapping("/logout")
    public ResponseEntity<RegisterResult> logout(
            @CookieValue(value = "token", required = false) String token,
            HttpServletResponse response) {

        if (token == null) {
            return ResponseEntity.ok(new RegisterResult(false, "You are not logged in"));
        }

        ResponseCookie cookie = sessionService.deleteCookie(token);

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new RegisterResult(true, ""));
    }





}



