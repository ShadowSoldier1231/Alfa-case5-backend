package com.project.main.service.component;


import com.project.main.dto.event.UserDeletedEvent;
import com.project.main.model.user.UserData;
import com.project.main.repository.cases.SolutionRepository;
import com.project.main.repository.user.*;
import com.project.main.service.common.S3StorageService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserDataCleanupListener {

    private final UserDataRepository userDataRepository;
    private final S3StorageService s3StorageService;
    private final SolutionRepository solutionRepository;
    private final AchievementRepository achievementRepository;
    private final UserFavoriteCaseRepository favoriteCaseRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final UserVerificationRepository verificationRepository;

    public UserDataCleanupListener(UserDataRepository userDataRepository,
                                   S3StorageService s3StorageService,
                                   SolutionRepository solutionRepository,
                                   AchievementRepository achievementRepository,
                                   UserFavoriteCaseRepository favoriteCaseRepository,
                                   UserPreferenceRepository preferenceRepository,
                                   LeaderboardRepository leaderboardRepository,
                                   UserVerificationRepository verificationRepository) {
        this.userDataRepository = userDataRepository;
        this.s3StorageService = s3StorageService;
        this.solutionRepository = solutionRepository;
        this.achievementRepository = achievementRepository;
        this.favoriteCaseRepository =favoriteCaseRepository;
        this.preferenceRepository = preferenceRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.verificationRepository = verificationRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserDeleted(UserDeletedEvent event) {
        Long userId = event.userId();

        UserData userData = userDataRepository.findById(userId).orElse(null);
        if (userData != null && userData.getAvatarUrl() != null && !userData.getAvatarUrl().isBlank()) {
            s3StorageService.deleteFile(userData.getAvatarUrl());
        }

        verificationRepository.deleteByUserId(userId);
        leaderboardRepository.deleteById(userId);
        preferenceRepository.deleteById(userId);
        favoriteCaseRepository.deleteByUserId(userId);
        userDataRepository.deleteById(userId);
        solutionRepository.deleteAllByUserId(userId);
        achievementRepository.deleteAllByUserId(userId);
    }
}
