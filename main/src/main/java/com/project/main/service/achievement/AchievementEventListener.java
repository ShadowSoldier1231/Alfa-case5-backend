package com.project.main.service.achievement;


import com.project.main.dto.event.SolutionSubmittedEvent;
import com.project.main.dto.event.WarningReceivedEvent;
import com.project.main.enums.Achievement;
import com.project.main.model.user.UserAchievement;
import com.project.main.repository.user.AchievementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AchievementEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AchievementEventListener.class);
    private final List<AchievementChecker> checkers;
    private final AchievementRepository achievementRepository;

    public AchievementEventListener(List<AchievementChecker> checkers,
                                    AchievementRepository achievementRepository) {
        this.checkers = checkers;
        this.achievementRepository = achievementRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSolutionSubmitted(SolutionSubmittedEvent event) {
        for (AchievementChecker checker : checkers) {
            try {
                checker.checkAndAward(event.getUserId(), event);
            } catch (Exception e) {
                logger.error("Error in achievement checker {}: {}", checker.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWarningReceived(WarningReceivedEvent event) {
        Long userId = event.userId();
        Long achievementId = Achievement.SCOUNDREL.getId();

        if (!achievementRepository.existsByUserIdAndAchievementId(userId, achievementId)) {
            try {
                achievementRepository.save(new UserAchievement(userId, achievementId));
                logger.info("User {} unlocked achievement: {}", userId, Achievement.SCOUNDREL.getName());
            } catch (Exception e) {
                logger.debug("Achievement {} already exists for user {}", achievementId, userId);
            }
        }
    }

}