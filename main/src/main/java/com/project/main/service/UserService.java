package com.project.main.service;

import com.project.main.dto.*;
import com.project.main.enums.GenderCode;
import com.project.main.enums.UserRole;
import com.project.main.exception.BadRequestException;
import com.project.main.exception.ConflictException;
import com.project.main.exception.InvalidCredentialsException;
import com.project.main.exception.NotFoundException;
import com.project.main.model.*;
import com.project.main.repository.*;
import org.apache.commons.lang3.tuple.Pair;
import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    private final S3StorageService s3StorageService;
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final VerificationService verificationService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       UserDataRepository userDataRepository,
                       LeaderboardRepository leaderboardRepository, VerificationService verificationService,
                       ApplicationEventPublisher eventPublisher, S3StorageService s3StorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDataRepository = userDataRepository;
        this.verificationService = verificationService;
        this.leaderboardRepository = leaderboardRepository;
        this.eventPublisher = eventPublisher;
        this.s3StorageService = s3StorageService;
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private void performDummyCheck() {
        passwordEncoder.matches("!@#$%^^&*()word", "##just##key##mash");
    }

    @Transactional
    public void updatePassword(Long id, String hashedPassword) {
        UserSetup user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setPassword(hashedPassword);
        userRepository.saveAndFlush(user);
    }

    @Transactional
    public void saveProfilePicture(Long userId, MultipartFile file) {
        UserData userData = userDataRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (userData.getAvatarUrl() != null && !userData.getAvatarUrl().isBlank()) {
            s3StorageService.deleteFile(userData.getAvatarUrl());
        }

        String avatarKey = s3StorageService.uploadFile(file, "avatars");

        userData.setAvatarUrl(avatarKey);
        userDataRepository.save(userData);
    }

    public void verifyUser(Long userId, Long verificationCode) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User does not exist");
        }
        if (verificationCode == null) {
            throw new BadRequestException("Verification code is required");
        }

        verificationService.verifyUser(userId, verificationCode);
    }

    @Transactional
    public void updateEmail(Long id, String email) {
        UserSetup user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setEmail(email);
    }

    public boolean passwordValidator(Long id, String password) {
        UserSetup user = userRepository.findById(id).orElse(null);
        if (user == null) {
            performDummyCheck();
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }

    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean userExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public void deleteUserByAdmin(Long userId) {
        UserSetup user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User does not exist"));

        eventPublisher.publishEvent(new UserDeletedEvent(userId));

        userRepository.delete(user);
    }

    @Transactional
    public Pair<String, Long> registerNewUser(RegisterRequest registerRequest, String hashedPassword) {
        UserSetup validUser = new UserSetup(
                hashedPassword,
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                UserRole.USER,
                null,
                false
        );
        validUser.setCurrentTime();
        this.userRepository.save(validUser);

        String verification = verificationService.generateVerification(
                validUser.getId(),
                registerRequest.getValidationMethod(),
                registerRequest.getEmail()
        );

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
                registerRequest.getUsername(),
                null
        );
        userDataRepository.save(userData);

        leaderboardRepository.save(new LeaderboardUser(validUser.getId(), 0L, 0L, 0L));

        return Pair.of(verification, validUser.getId());
    }

    @Transactional
    public Long createAdminUser(AdminUserCreateRequest req, String hashedPassword) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new ConflictException("Username is already taken");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Email is already taken");
        }

        UserSetup newUser = new UserSetup(
                hashedPassword,
                req.getUsername(),
                req.getEmail(),
                req.getRole() != null ? req.getRole() : UserRole.USER,
                null,
                true
        );
        newUser.setCurrentTime();
        userRepository.save(newUser);

        GenderCode gender = (req.getGender() != null) ? req.getGender() : GenderCode.NOT_STATED;
        UserData userData = new UserData(
                newUser.getId(),
                req.getFirstName(),
                req.getLastName(),
                req.getBirthdate(),
                req.getStatus(),
                req.getCityId(),
                req.getMiddleName(),
                gender,
                req.getUsername(),
                null
        );
        userDataRepository.save(userData);

        leaderboardRepository.save(new LeaderboardUser(newUser.getId(), 0L, 0L, 0L));

        return newUser.getId();
    }

    @Transactional
    public void updateAdminUser(Long id, AdminUserUpdateRequest req) {
        UserSetup user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        UserData userData = userDataRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User data not found"));

        if (req.getRole() != null) user.setRole(req.getRole());
        if (req.getBannedUntil() != null) user.setBannedUntil(req.getBannedUntil());
        if (req.getIsVerified() != null) user.setVerified(req.getIsVerified());
        if (req.getEmail() != null) {
            if (!req.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(req.getEmail())) {
                throw new ConflictException("Email is already taken");
            }
            user.setEmail(req.getEmail());
        }

        if (req.getFirstName() != null) userData.setFirstName(req.getFirstName());
        if (req.getLastName() != null) userData.setLastName(req.getLastName());
        if (req.getMiddleName() != null) userData.setMiddleName(req.getMiddleName());
        if (req.getNickName() != null) userData.setNickName(req.getNickName());
        if (req.getBirthdate() != null) userData.setBirthdate(req.getBirthdate());
        if (req.getStatus() != null) userData.setStatus(req.getStatus());
        if (req.getCityId() != null) userData.setCityId(req.getCityId());
        if (req.getGender() != null) userData.setGender(req.getGender());

        userRepository.save(user);
        userDataRepository.save(userData);
    }

    @Transactional
    public void updateUserParams(Long userId, ChangeParamsRequest changeRequest) {
        UserData realUser = userDataRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User does not exist"));

        if (changeRequest.getCityId() != null) realUser.setCityId(changeRequest.getCityId());
        if (changeRequest.getBirthdate() != null) realUser.setBirthdate(changeRequest.getBirthdate());
        if (changeRequest.getStatus() != null) realUser.setStatus(changeRequest.getStatus());
        if (changeRequest.getNickName() != null) realUser.setNickName(changeRequest.getNickName());
        if (changeRequest.getFirstName() != null && !changeRequest.getFirstName().isEmpty()) realUser.setFirstName(changeRequest.getFirstName());
        if (changeRequest.getLastName() != null && !changeRequest.getLastName().isEmpty()) realUser.setLastName(changeRequest.getLastName());

        userDataRepository.save(realUser);
    }

    @Transactional
    public Long authenticateUser(LoginRequest loginRequest) {
        UserSetup realUser = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);

        if (realUser == null) {
            performDummyCheck();
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), realUser.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (!realUser.isVerified()) {
            throw new InvalidCredentialsException("Account is not verified");
        }

        if (realUser.getBannedUntil() != null) {
            if (realUser.getBannedUntil().isAfter(LocalDateTime.now())) {
                throw new InvalidCredentialsException("User is still banned");
            }
            realUser.setBannedUntil(null);
            userRepository.save(realUser);
        }

        return realUser.getId();
    }
}