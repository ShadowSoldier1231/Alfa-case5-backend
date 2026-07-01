package com.project.main.controller;




import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.InputUser;
import com.project.main.dto.RegisterResult;
import com.project.main.enums.GenderCode;
import com.project.main.enums.UserRole;
import com.project.main.enums.UserStatus;
import com.project.main.model.*;
import com.project.main.repository.LeaderboardRepository;
import com.project.main.repository.UserDataRepository;
import com.project.main.repository.UserRepository;
import com.project.main.repository.UserSessionRepository;
import com.project.main.service.SessionService;
import com.project.main.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.validator.routines.EmailValidator;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;




@RestController
@RequestMapping("/api/v1/auth")
public class LoginApiController {




    private final UserService userService;
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;
    private final UserSessionRepository sessionRepository;
    private final LeaderboardRepository leaderboardRepository;

    public LoginApiController(UserRepository userRepository, UserService userService, UserSessionRepository userSessionRepository,
                              UserDataRepository userDataRepository, SessionService sessionService,
                              LeaderboardRepository leaderboardRepository) {
        this.userDataRepository = userDataRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.sessionRepository = userSessionRepository;
        this.sessionService = sessionService;
        this.leaderboardRepository = leaderboardRepository;

    }



    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/changeemail")
    public ResponseEntity<RegisterResult> changeEmail(@RequestBody InputUser user,
                                                      @CookieValue(value = "token", required = false) String token){
        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getFirst();
        if(!cookieCheck.getSuccess()){
            return ResponseEntity.ok(cookieCheck);
        }
        UserSession session = sessionPair.getSecond();
        if(user.getEmail() == null){
            return ResponseEntity.ok(new RegisterResult(false, "Email cannot be blank"));
        }
        if(user.getEmail().isBlank()){
            return ResponseEntity.ok(new RegisterResult(false, "Email cannot be blank"));
        }
        if(userRepository.existsByEmail(user.getEmail())) return ResponseEntity.ok(new RegisterResult(false, "This email address is already taken"));
        if(EmailValidator.getInstance(true).isValid(user.getEmail())) {
            userService.updateEmail(session.getUserId(), user.getEmail());
            return ResponseEntity.ok(new RegisterResult(true, ""));
        }else {
            return ResponseEntity.ok( new RegisterResult(false, "This email address is invalid"));
        }

    }
    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/changeparams")
    public ResponseEntity<RegisterResult> changeParams(@RequestBody InputUser user, @CookieValue(value = "token", required = false) String token){
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

        if(user.getCityId() != null){
            realUser.setCityId(user.getCityId());
        }
        if(user.getBirthdate() != null){
            realUser.setBirthdate(user.getBirthdate());
        }
        if(user.getStatus() != null){
            switch (user.getStatus()){
                case WORKER:
                    realUser.setStatus(UserStatus.WORKER);
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
        if (user.getFirstName() != null){
            if (!user.getFirstName().isEmpty()) {
                realUser.setFirstName(user.getFirstName());
            }
        }
        if(user.getLastName() != null){
            if (!user.getLastName().isEmpty()){
                realUser.setLastName(user.getLastName());
            }
        }
        if(user.getProfilePictureId() != null){
            realUser.setProfilePictureId(user.getProfilePictureId());
        }

        userDataRepository.save(realUser);
        return ResponseEntity.ok(new RegisterResult(true, ""));

    }
    @JsonView(Views.RegisterResultPartial.class)
    @Transactional
    @PostMapping("/resetpassword")
    ResponseEntity<RegisterResult> resetPassword( @RequestBody InputUser user, @CookieValue(value = "token", required = false) String token, HttpServletResponse response){

        Pair<RegisterResult, UserSession> sessionPair = sessionService.checkCookie(token);
        RegisterResult cookieCheck = sessionPair.getFirst();
        if(!cookieCheck.getSuccess()){
            return ResponseEntity.ok(cookieCheck);
        }
        UserSession session = sessionPair.getSecond();

        if(userService.passwordValidator(session.getUserId(), user.getOldPassword() )) {

            switch (userService.checkPassword(user.getNewPassword())){

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
                    userService.updatePassword(session.getUserId(), user.getNewPassword());
                    sessionRepository.deleteByUserId(session.getUserId());

                    sessionRepository.deleteByToken(token);

                    ResponseCookie cookie = ResponseCookie.from("token", "")
                            .httpOnly(true)
                            .secure(true)
                            .path("/")
                            .maxAge(0)
                            .sameSite("Lax")
                            .build();

                    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                    return ResponseEntity.ok(new RegisterResult(true, ""));
            }



        } else {
            return ResponseEntity.ok(new RegisterResult(false, "Incorrect password"));
        }


    }


    @JsonView(Views.RegisterResultFull.class)
    @PostMapping("/register")
    public ResponseEntity<RegisterResult> registerUser(@RequestBody InputUser user){
        String botUrl = "";
        switch (userService.checkPassword(user.getPassword())){

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
        switch (userService.checkUsername(user.getUsername())){
            case TOO_LONG:
                return ResponseEntity.ok(new RegisterResult(false, "Username cannot be longer than 20 characters"));
            case TOO_SHORT:
                return ResponseEntity.ok(new RegisterResult(false, "Username cannot be shorter than 3 characters"));
            case EMPTY:
                return ResponseEntity.ok(new RegisterResult(false, "Username cannot be empty"));
            case SPACE:
                return  ResponseEntity.ok(new RegisterResult(false, "Username cannot contain spaces"));
            case OK: {

                if (userRepository.existsByEmail(user.getEmail())){
                    return  ResponseEntity.ok(new RegisterResult(false, "This email address is already taken"));
                }else if (userRepository.existsByUsername(user.getUsername())){
                    return  ResponseEntity.ok(new RegisterResult(false, "This username is already taken"));
                }else {
                    if(! EmailValidator.getInstance(true).isValid(user.getEmail())) {
                        return  ResponseEntity.ok(new RegisterResult(false, "This email address is invalid"));
                    }


                    UserSetup validUser = new UserSetup(user.getPassword(),
                            user.getUsername(), user.getEmail(), UserRole.USER, null);
                    botUrl = userService.save(validUser);

                    if (validUser.getId() != null) {

                        userDataRepository.save(new UserData(validUser.getId(), user.getFirstName(), user.getLastName(),
                                user.getBirthdate(), user.getStatus(), user.getCityId(), (long) 0,
                                (user.getGender() != null) ? user.getGender() : GenderCode.NOT_STATED)
                        );
                        leaderboardRepository.save( new LeaderboardUser(validUser.getId(), (long) 0,
                                (long) 0, (long) 0, new ArrayList<Long>(), 0L)
                        );
                    }else {

                        userDataRepository.save(new UserData(userRepository.findByUsername(validUser.getUsername()).get().getId(),
                                user.getFirstName(), user.getLastName(),
                                user.getBirthdate(), user.getStatus(), user.getCityId(), (long) 0,
                                (user.getGender() != null) ? user.getGender() : GenderCode.NOT_STATED)
                        );
                        leaderboardRepository.save( new LeaderboardUser(userRepository.findByUsername(validUser.getUsername()).get().getId(),
                                (long) 0, (long) 0, (long) 0, new ArrayList<Long>(), 0L)
                        );
                    }

                }
            }
        }
        return ResponseEntity.ok( new RegisterResult(true, "", botUrl));

    }

    @JsonView(Views.RegisterResultPartial.class)
    @PostMapping("/login")
    public ResponseEntity<RegisterResult> loginUser(@RequestBody UserSetup user,
                                    @CookieValue(value = "token", required = false) String token, HttpServletResponse response){

        if (token != null) {
            return ResponseEntity.ok(new RegisterResult(false, "You are already logged in"));
        }

        if(user.getPassword().isBlank()){
            return ResponseEntity.ok(new RegisterResult(false, "Password cannot be empty"));
        }else if(user.getUsername().isBlank()){
            return ResponseEntity.ok(new RegisterResult(false, "Username cannot be empty"));
        }
        else{

            if ( userRepository.existsByUsername(user.getUsername()) ) {
                if (userService.loginUser(user)) {

                    if(user.getBannedUntil() != null){
                        if(user.getBannedUntil().isAfter(LocalDateTime.now())){
                            return ResponseEntity.ok(new RegisterResult(false, "User is still banned"));
                        }
                        user.setBannedUntil(null);
                        userRepository.save(user);
                    }



                    byte[] randomBytes = new byte[32];
                    new SecureRandom().nextBytes(randomBytes);
                    String secureValue = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

                    ResponseCookie cookie = ResponseCookie.from("token", secureValue)
                            .httpOnly(true)
                            .secure(true)
                            .path("/")
                            .maxAge(7200)
                            .sameSite("Lax")
                            .build();
                    UserSession session = new UserSession();
                    session.setToken(secureValue);
                    session.setUserId(userRepository.findByUsername(user.getUsername()).get().getId());
                    session.setExpiryDate(LocalDateTime.now().plusHours(2));
                    sessionRepository.save(session);
                    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                    return ResponseEntity.ok(new RegisterResult(true, ""));
                } else {
                    return ResponseEntity.ok(new RegisterResult(false, "Incorrect password"));
                }
            } else {
                return ResponseEntity.ok(new RegisterResult(false, "Account does not exist"));
            }
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


        sessionRepository.deleteByToken(token);

        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new RegisterResult(true, ""));
    }





}



