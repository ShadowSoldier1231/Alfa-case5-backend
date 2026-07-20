package com.project.main.service;

import com.project.main.dto.ChangeParamsRequest;
import com.project.main.dto.LoginRequest;
import com.project.main.dto.RegisterRequest;
import com.project.main.enums.GenderCode;
import com.project.main.enums.UserRole;
import com.project.main.model.*;
import com.project.main.repository.*;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;




@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDataRepository userDataRepository;
    private final UserAvatarRepository userAvatarRepository;
    private final LeaderboardRepository leaderboardRepository;

    public UserService(UserRepository userRepository,
                       UserDataRepository userDataRepository, UserAvatarRepository userAvatarRepository,
                        LeaderboardRepository leaderboardRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userDataRepository = userDataRepository;
        this.userAvatarRepository = userAvatarRepository;

        this.leaderboardRepository = leaderboardRepository;

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
    public void updatePassword(Long id, String newPassword) throws Exception {
        UserSetup user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("user is null"));
        user.setPassword(this.passwordEncoder.encode(newPassword));
        userRepository.saveAndFlush(user);
    }

    @Transactional
    public void saveProfilePicture(Long userId, byte[] imageBytes) {
        UserAvatar avatar = new UserAvatar(userId, imageBytes);
        this.userAvatarRepository.save(avatar);
    }


    @Transactional
    public void updateEmail(Long id, String email) {
        UserSetup user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        user.setEmail(email);
    }

    public boolean loginUser(LoginRequest loginRequest, UserSetup realUser) {
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            passwordEncoder.matches("!@#$%^^&*()word", "##just##key##mash");
            return false;
        }
        return passwordEncoder.matches(loginRequest.getPassword(), realUser.getPassword());
    }

    public boolean passwordValidator(Long id, String password) {
        UserSetup user = userRepository.findById(id).orElse(null);
        if (user == null) {
            passwordEncoder.matches("!@#$%^^&*()word", "##just##key##mash");
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Transactional
    public boolean verifyUser(String token, Long chatId) {
        UserSetup user = userRepository.findByTelegramVerificationToken(token).orElse(null);
        if (user != null) {
            user.setTelegramId(chatId);
            user.setTelegramVerificationToken(null);
            return true;
        }
        return false;
    }

    public boolean userExistsByEmail(String email){
        return userRepository.existsByEmail(email);
    }
    public boolean userExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public String registerNewUser(RegisterRequest registerRequest) {

        UserSetup validUser = new UserSetup(
                registerRequest.getPassword(),
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                UserRole.USER,
                null
        );


        String botUrl = this.save(validUser);


        GenderCode gender = (registerRequest.getGender() != null) ? registerRequest.getGender() : GenderCode.NOT_STATED;
        UserData userData = new UserData(
                validUser.getId(),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getBirthdate(),
                registerRequest.getStatus(),
                registerRequest.getCityId(),
                registerRequest.getMiddleName(),
                gender,
                registerRequest.getUsername()
        );
        userDataRepository.save(userData);


        LeaderboardUser leaderboardUser = new LeaderboardUser(validUser.getId(), 0L, 0L, 0L, 0L);
        leaderboardRepository.save(leaderboardUser);

        return botUrl;
    }


    @Transactional
    public void updateUserParams(Long userId, ChangeParamsRequest changeRequest) {

        UserData realUser = userDataRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User does not exist"));

        if (changeRequest.getCityId() != null) {
            realUser.setCityId(changeRequest.getCityId());
        }
        if (changeRequest.getBirthdate() != null) {
            realUser.setBirthdate(changeRequest.getBirthdate());
        }
        if (changeRequest.getStatus() != null) {
            realUser.setStatus(changeRequest.getStatus());
        }
        if (changeRequest.getNickName() != null) {
            realUser.setNickName(changeRequest.getNickName());
        }
        if (changeRequest.getFirstName() != null && !changeRequest.getFirstName().isEmpty()) {
            realUser.setFirstName(changeRequest.getFirstName());
        }
        if (changeRequest.getLastName() != null && !changeRequest.getLastName().isEmpty()) {
            realUser.setLastName(changeRequest.getLastName());
        }

        userDataRepository.save(realUser);
    }

    @Transactional
    public Long authenticateUser(LoginRequest loginRequest) {
        UserSetup realUser = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);

        if (realUser == null) {
            passwordEncoder.matches("!@#$%^^&*()word", "##just##key##mash");
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), realUser.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (realUser.getBannedUntil() != null) {
            if (realUser.getBannedUntil().isAfter(LocalDateTime.now())) {
                throw new BadCredentialsException("User is still banned");
            }
            realUser.setBannedUntil(null);
            userRepository.save(realUser);
        }

        return realUser.getId();
    }



}
