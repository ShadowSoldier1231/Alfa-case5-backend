package com.project.main.service.user;

import com.project.main.dto.common.PageResponse;
import com.project.main.dto.event.ForgotPasswordInitEvent;
import com.project.main.dto.event.ForgotUsernameEvent;
import com.project.main.dto.event.UserDeletedEvent;
import com.project.main.dto.user.*;
import com.project.main.dto.auth.LoginRequest;
import com.project.main.dto.auth.RegisterRequest;
import com.project.main.dto.auth.ResendEmailRequest;
import com.project.main.enums.GenderCode;
import com.project.main.enums.UserRole;
import com.project.main.enums.UserStatus;
import com.project.main.exception.*;
import com.project.main.model.user.LeaderboardUser;
import com.project.main.model.user.UserData;
import com.project.main.model.user.UserSetup;
import com.project.main.repository.user.LeaderboardRepository;
import com.project.main.repository.user.UserDataRepository;
import com.project.main.repository.user.UserRepository;
import com.project.main.service.auth.VerificationRateLimitService;
import com.project.main.service.auth.VerificationService;
import com.project.main.service.common.FetchingService;
import com.project.main.service.common.S3StorageService;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.tuple.Pair;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
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
    private final FetchingService fetchingService;
    private final VerificationRateLimitService rateLimitService;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private static String DUMMY_HASHED_PASSWORD = "$2a$10$0MB4zN/nNgjOwWGR4vddk.2CDWaOMZAUdWAJ0p4XC9VS.9aWkW5bu";
    private static final String DUMMY_PASSWORD = "aLongCharSequence";

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       UserDataRepository userDataRepository,
                       LeaderboardRepository leaderboardRepository, VerificationService verificationService,
                       ApplicationEventPublisher eventPublisher, S3StorageService s3StorageService,
                       FetchingService fetchingService,
                       VerificationRateLimitService rateLimitService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDataRepository = userDataRepository;
        this.verificationService = verificationService;
        this.leaderboardRepository = leaderboardRepository;
        this.eventPublisher = eventPublisher;
        this.s3StorageService = s3StorageService;
        this.fetchingService = fetchingService;
        this.rateLimitService = rateLimitService;
    }

    @PostConstruct
    private void initDummyHash() {
        DUMMY_HASHED_PASSWORD = passwordEncoder.matches(DUMMY_PASSWORD, DUMMY_HASHED_PASSWORD)
                ? DUMMY_HASHED_PASSWORD
                : passwordEncoder.encode(DUMMY_PASSWORD);
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private void performDummyCheck() {
        passwordEncoder.matches(DUMMY_PASSWORD, DUMMY_HASHED_PASSWORD);
    }

    @Transactional
    public void updatePassword(Long id, String hashedPassword) {
        UserSetup user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setPassword(hashedPassword);
        userRepository.saveAndFlush(user);
    }
    @Transactional
    public Long confirmPasswordReset(String email, String username, Long code, String hashedPassword) {
        UserSetup user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid email or username"));

        if (!user.getUsername().equalsIgnoreCase(username)) {
            throw new BadRequestException("Invalid email or username");
        }

        verificationService.validateAndConsumeResetCode(user.getId(), code);

        updatePassword(user.getId(), hashedPassword);

        return user.getId();
    }

    @Transactional
    public void saveProfilePicture(Long userId, MultipartFile file) {
        UserData userData = userDataRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));


        String avatarKey = s3StorageService.uploadFile(file, "avatars");

        if (userData.getAvatarUrl() != null && !userData.getAvatarUrl().isBlank()) {
            s3StorageService.deleteFile(userData.getAvatarUrl());
        }
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
        user.setEmail(email.toLowerCase());
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
                registerRequest.getEmail().toLowerCase(),
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
        String lowerEmail = req.getEmail().toLowerCase();
        if (userRepository.existsByEmail(lowerEmail)) {
            throw new ConflictException("Email is already taken");
        }

        UserSetup newUser = new UserSetup(
                hashedPassword,
                req.getUsername(),
                lowerEmail,
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

    public void processForgotUsername(String email, String clientIp) {
        eventPublisher.publishEvent(new ForgotUsernameEvent(email.toLowerCase(), clientIp));
    }

    @Async("taskExecutor")
    public void initiatePasswordReset(String email, String username, String clientIp) {
        try {
            userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
                if (user.getUsername().equalsIgnoreCase(username)) {
                    rateLimitService.checkCanSendEmailForUser(user.getId());
                    long code = verificationService.createPasswordResetCode(user.getId());
                    eventPublisher.publishEvent(new ForgotPasswordInitEvent(user.getEmail(), code, clientIp));
                }
            });
        } catch (Exception e) {
            logger.error("Error processing password reset request for email: {}", email, e);
        }
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
            String lowerEmail = req.getEmail().toLowerCase();
            if (!lowerEmail.equals(user.getEmail()) && userRepository.existsByEmail(lowerEmail)) {
                throw new ConflictException("Email is already taken");
            }
            user.setEmail(lowerEmail);
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
        if(changeRequest.getMiddleName() != null) realUser.setMiddleName(changeRequest.getMiddleName());
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


    @Transactional
    public Pair<String, Long> resendEmailOrThrow(ResendEmailRequest request) {
        UserSetup realUser = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (realUser == null) {
            performDummyCheck();
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (!passwordEncoder.matches(request.getPassword(), realUser.getPassword())) {
            performDummyCheck();
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (realUser.isVerified()) {
            throw new BadRequestException("Account is already verified");
        }

        if (realUser.getBannedUntil() != null) {
            if (realUser.getBannedUntil().isAfter(LocalDateTime.now())) {
                throw new InvalidCredentialsException("User is still banned");
            }
            realUser.setBannedUntil(null);
        }


        String newEmail = request.getEmail().toLowerCase();

        if (!newEmail.equalsIgnoreCase(realUser.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new ConflictException("This email address is already taken");
            }
            realUser.setEmail(newEmail);
        }

        userRepository.save(realUser);
        rateLimitService.checkCanSendEmailForUser(realUser.getId());
        String verification = verificationService.generateVerification(
                realUser.getId(),
                request.getValidationMethod(),
                realUser.getEmail()
        );

        return Pair.of(verification, realUser.getId());
    }


    public PageResponse<UserListItem> getAdminUsers(int page, int size, String search, String sort) {
        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }
        if (page < 0) {
            throw new BadRequestException("Page cannot be negative");
        }

        Sort sortBy = buildAdminUserSort(sort);

        Pageable pageable = PageRequest.of(page, size, sortBy);
        String searchTerm = null;

        if (search != null && !search.isBlank()) {
            searchTerm = escapeLikeWildcards(search.trim());
        }

        if (searchTerm != null && searchTerm.length() > 200) {
            throw new BadRequestException("Search query is too long");
        }


        Page<Object[]> userPage = userRepository.findUsersForAdmin(searchTerm, pageable);


        List<UserListItem> items = userPage.getContent().stream().map(row -> {
            Long id = ((Number) row[0]).longValue();
            String username = (String) row[1];
            String email = (String) row[2];
            String nickName = (String) row[3];


            String role = row[4] != null ? row[4].toString() : null;

            String status = row[5] != null ? row[5].toString() : null;;

            boolean isVerified = toBoolean(row[6]);


            LocalDateTime bannedUntil = toLocalDateTime(row[7]);

            return new UserListItem(id, username, email, nickName, role, status, isVerified, bannedUntil);
        }).collect(Collectors.toList());

        return new PageResponse<>(
                items,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public UserDetailsResponse getUserDetails(Long userId) {
        UserSetup user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserProfile profile = fetchingService.getMyProfile(userId);

        if (profile == null) {
            logger.error("Failed to load user profile for userId={}", userId);
            throw new InternalServerErrorException("Profile could not be loaded");
        }


        return new UserDetailsResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                profile.getFirstName(),
                profile.getMiddleName(),
                profile.getLastName(),
                profile.getNickName(),
                profile.getStatus(),
                profile.getGender(),
                profile.getBirthdate(),
                profile.getCityName() != null ? profile.getCityName() : "not_set",
                profile.getRegionName() != null ? profile.getRegionName() : "not_set",
                profile.getScore() != null ? profile.getScore() : 0L,
                profile.getPlacement() != null ? profile.getPlacement() : 0L,
                profile.getAvatarUrl(),
                user.getRole(),
                user.isVerified(),
                user.getBannedUntil(),
                user.getCreationDate()
        );
    }


    private Sort buildAdminUserSort(String sort) {
        Sort sortBy = Sort.by(Sort.Direction.DESC, "creation_date");

        if (sort == null || sort.isBlank()) {
            return sortBy;
        }

        String[] sortParts = sort.split(",");
        String property = sortParts[0].trim();

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1].trim())) {
            direction = Sort.Direction.DESC;
        }

        String sortColumn = switch (property.toLowerCase()) {
            case "id" -> "id";
            case "username" -> "username";
            case "email" -> "email";
            case "nickname", "nick_name", "nick" -> "nick_name";
            case "role" -> "role";
            case "status" -> "status";
            case "verified", "isverified", "is_verified" -> "is_verified";
            case "banneduntil", "banned_until", "banned" -> "banned_until";
            case "createdat", "created_at", "creationdate", "creation_date" -> "creation_date";
            default -> "creation_date";
        };

        return Sort.by(direction, sortColumn);
    }
    private String escapeLikeWildcards(String value) {
        if (value == null) {
            return null;
        }

        return value
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }
    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }

        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime();
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate().atStartOfDay();
        }

        if (value instanceof java.time.OffsetDateTime odt) {
            return odt.toLocalDateTime();
        }

        if (value instanceof java.time.Instant instant) {
            return instant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        }

        if (value instanceof java.util.Date utilDate) {
            return utilDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
        }

        logger.warn(
                "Unsupported datetime value: value='{}', class='{}'",
                value,
                value.getClass().getName()
        );

        return null;
    }

    private Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean b) {
            return b;
        }

        if (value instanceof Number n) {
            return n.intValue() != 0;
        }

        String s = value.toString().trim().toLowerCase();

        return switch (s) {
            case "true", "t", "1", "yes", "y" -> true;
            case "false", "f", "0", "no", "n" -> false;
            default -> null;
        };
    }
}