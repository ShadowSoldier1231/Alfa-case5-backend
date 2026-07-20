package com.project.main.service;


import com.project.main.dto.UserDeletedEvent;
import com.project.main.repository.AchievementRepository;
import com.project.main.repository.SolutionRepository;
import com.project.main.repository.UserAvatarRepository;
import com.project.main.repository.UserDataRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserDataCleanupListener {

    private final UserDataRepository userDataRepository;
    private final UserAvatarRepository userAvatarRepository;
    private final SolutionRepository solutionRepository;
    private final AchievementRepository achievementRepository;

    public UserDataCleanupListener(UserDataRepository userDataRepository,
                                   UserAvatarRepository userAvatarRepository,
                                   SolutionRepository solutionRepository,
                                   AchievementRepository achievementRepository) {
        this.userDataRepository = userDataRepository;
        this.userAvatarRepository = userAvatarRepository;
        this.solutionRepository = solutionRepository;
        this.achievementRepository = achievementRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserDeleted(UserDeletedEvent event) {
        Long userId = event.userId();

        userDataRepository.deleteById(userId);
        userAvatarRepository.deleteByUserId(userId);
        solutionRepository.deleteAllByUserId(userId);
        achievementRepository.deleteAllByUserId(userId);
    }
}
