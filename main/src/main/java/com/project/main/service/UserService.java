package com.project.main.service;

import com.project.main.*;
import com.project.main.enums.ValidPasswordStatus;
import com.project.main.enums.ValidUsernameStatus;
import com.project.main.model.City;
import com.project.main.model.LeaderboardUser;
import com.project.main.model.UserSetup;
import com.project.main.repository.*;

import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CityRepository cityRepository;
    private final UserSessionRepository sessionRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final UserDataRepository userDataRepository;


    public UserService(UserRepository userRepository, CityRepository cityRepository,
                       UserSessionRepository sessionRepository, LeaderboardRepository leaderboardRepository,
                       UserDataRepository userDataRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.cityRepository = cityRepository;
        this.sessionRepository = sessionRepository;
        this.userDataRepository = userDataRepository;
        this.leaderboardRepository = leaderboardRepository;
    }

    public String  save(UserSetup user) {
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


    public boolean loginUser(UserSetup user) {

        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return false;
        }
        UserSetup realUser = userRepository.findByUsername(user.getUsername()).orElse(null);
        if (realUser == null) return  false;

        return passwordEncoder.matches(user.getPassword(), realUser.getPassword());
    }


    public boolean passwordValidator(Long id, String password) {

        return (passwordEncoder.matches(password, userRepository.findById(id).get().getPassword()));
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        userDataRepository.deleteById(id);
        leaderboardRepository.deleteById(id);
        sessionRepository.deleteByUserId(id);
    }

    @Transactional
    public String banUser(Long userId){

        LeaderboardUser leaderboardUser = leaderboardRepository.findById(userId).orElse(null);

        if(leaderboardUser == null) return  "User does not exist";

        if (leaderboardUser.getBanCount() > 3){
            deleteUser(userId);
            return  "User no longer exists";
        }
        UserSetup user = userRepository.findById(userId).orElse(null);
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


    Long getCityId(String cityName){
        if(cityName == null) return (long) -1;
        City city = cityRepository.findByCityName(cityName).orElse(null);
        if (city == null) return -1L;
        return city.getId();
    }

    public ValidPasswordStatus checkPassword(String password)
    {

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

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void clearExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.deleteByExpiryDateBefore(now);
    }


    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateLeaderboard() {
        leaderboardRepository.updateAllPlacements();
    }

}
