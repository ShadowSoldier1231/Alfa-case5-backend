package com.project.main.service;

import com.project.main.*;
import com.project.main.dto.LoginRequest;
import com.project.main.dto.UserDeletedEvent;
import com.project.main.enums.ValidPasswordStatus;
import com.project.main.enums.ValidUsernameStatus;

import com.project.main.model.LeaderboardUser;

import com.project.main.model.UserSetup;
import com.project.main.repository.*;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.time.LocalDateTime;



@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final LeaderboardRepository leaderboardRepository;
    private final UserDataRepository userDataRepository;


    public UserService(UserRepository userRepository,
                        LeaderboardRepository leaderboardRepository,
                       UserDataRepository userDataRepository, ApplicationEventPublisher eventPublisher) {

        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userDataRepository = userDataRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.eventPublisher = eventPublisher;
    }

    public String save(UserSetup user) {
        user.setCurrentTime();
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));

        String token = UUID.randomUUID().toString();
        user.setTelegramVerificationToken(token);

        this.userRepository.save(user);
        return "https://t.me/alfa_auth_verification_bot?start=" + token;
    }

    @Transactional
    public void updatePassword(Long id, String newPassword) {

        UserSetup user = userRepository.findById(id).get();
        user.setPassword(this.passwordEncoder.encode(newPassword));
        userRepository.saveAndFlush(user);
    }

    public void updateEmail(Long id, String email) {

        UserSetup user = userRepository.findById(id).get();
        user.setEmail(email);
        this.userRepository.save(user);
    }


    public boolean loginUser(LoginRequest loginRequest, UserSetup realUser) {

        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            boolean check = passwordEncoder.matches("!@#$%^^&*()word","##just##key##mash");
            return false;
        }


        return passwordEncoder.matches(loginRequest.getPassword(), realUser.getPassword());
    }



    public boolean passwordValidator(Long id, String password) {

        UserSetup user = userRepository.findById(id).orElse(null);
        if(user == null){
            boolean check = passwordEncoder.matches("!@#$%^^&*()word","##just##key##mash");
            return false;
        }

        return passwordEncoder.matches(password, user.getPassword());
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        userDataRepository.deleteById(id);
        leaderboardRepository.deleteById(id);
        eventPublisher.publishEvent(new UserDeletedEvent(id));
    }

    @Transactional
    public String banUser(LeaderboardUser leaderboardUser){


        if(leaderboardUser == null) return  "User does not exist";

        if (leaderboardUser.getBanCount() > 3){
            deleteUser(leaderboardUser.getUserId());
            return  "User no longer exists";
        }
        UserSetup user = userRepository.findById(leaderboardUser.getUserId()).orElse(null);
        if(user == null){
            return  "User does not exist";
        }
        leaderboardUser.setBanCount(leaderboardUser.getBanCount() + 1L);
        leaderboardUser.setWarningsCount(0L);
        user.setBannedUntil(LocalDateTime.now().plusMonths(2));
        leaderboardRepository.save(leaderboardUser);
        userRepository.save(user);

        return "User is now banned";
    }


    public ValidPasswordStatus checkPassword(String password)
    {
        if(password ==null) return ValidPasswordStatus.EMPTY;

        if(password.isBlank()) {
            return ValidPasswordStatus.EMPTY;
        }else if(password.length() < 8){
            return ValidPasswordStatus.TOO_SHORT;
        }else if(password.length() > 30){
            return ValidPasswordStatus.TOO_LONG;
        }else{
            String spec = "!@#$%^&*()_-+=;:/?|\\<>{}[]";
            boolean found = false;
            for (int i = 0; i < spec.length(); i++){
                if (password.indexOf(spec.charAt(i)) >= 0){
                    found = true;
                    break;
                }
            }
            if (!found) {
                return ValidPasswordStatus.NO_SPECIAL_SYMBOL;
            }

            found = false;
            for (int i = 0; i < password.length(); i++){
                char temp = password.charAt(i);
                if (Character.isDigit(temp)) {
                    found = true;
                }
            }
            if (!found) {
                return ValidPasswordStatus.NO_DIGITS;
            }
        }
        return ValidPasswordStatus.OK;
    }

    public ValidUsernameStatus checkUsername(String username){

        if(username == null) return  ValidUsernameStatus.EMPTY;

        if(username.length() < 3){
            return  ValidUsernameStatus.TOO_SHORT;
        }else if(username.length() > 20){
            return  ValidUsernameStatus.TOO_LONG;
        }else if(username.isBlank()){
            return  ValidUsernameStatus.EMPTY;
        }else if (username.indexOf(' ') >= 0){
            return ValidUsernameStatus.SPACE;
        }
        return ValidUsernameStatus.OK;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateLeaderboard() {
        leaderboardRepository.updateAllPlacements();
    }

}
