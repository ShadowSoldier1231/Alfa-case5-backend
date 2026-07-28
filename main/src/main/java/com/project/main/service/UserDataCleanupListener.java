package com.project.main.service;


import com.project.main.dto.UserDeletedEvent;
import com.project.main.model.UserData;
import com.project.main.repository.AchievementRepository;
import com.project.main.repository.SolutionRepository;
import com.project.main.repository.UserDataRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserDataCleanupListener {

    private final UserDataRepository userDataRepository;
    private final  S3StorageService s3StorageService;
    private final SolutionRepository solutionRepository;
    private final AchievementRepository achievementRepository;

    public UserDataCleanupListener(UserDataRepository userDataRepository,
                                   S3StorageService s3StorageService,
                                   SolutionRepository solutionRepository,
                                   AchievementRepository achievementRepository) {
        this.userDataRepository = userDataRepository;
        this.s3StorageService = s3StorageService;
        this.solutionRepository = solutionRepository;
        this.achievementRepository = achievementRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserDeleted(UserDeletedEvent event) {
        Long userId = event.userId();

        UserData userData = userDataRepository.findById(userId).orElse(null);
        if (userData != null && userData.getAvatarUrl() != null && !userData.getAvatarUrl().isBlank()) {
            s3StorageService.deleteFile(userData.getAvatarUrl());
        }


        userDataRepository.deleteById(userId);
        solutionRepository.deleteAllByUserId(userId);
        achievementRepository.deleteAllByUserId(userId);
    }
}
