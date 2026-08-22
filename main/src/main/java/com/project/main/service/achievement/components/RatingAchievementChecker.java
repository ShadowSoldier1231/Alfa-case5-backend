package com.project.main.service.achievement.components;

import com.project.main.dto.event.SolutionSubmittedEvent;
import com.project.main.enums.Achievement;
import com.project.main.model.user.UserAchievement;
import com.project.main.repository.cases.SolutionRepository;
import com.project.main.repository.user.AchievementRepository;
import com.project.main.service.achievement.AchievementChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Component
public class RatingAchievementChecker implements AchievementChecker {

    private static final Logger logger = LoggerFactory.getLogger(RatingAchievementChecker.class);
    private final AchievementRepository achievementRepository;
    private final SolutionRepository solutionRepository;

    private static final Long SOLVE_THRESHOLD = 70L;

    public RatingAchievementChecker(AchievementRepository achievementRepository, SolutionRepository solutionRepository) {
        this.achievementRepository = achievementRepository;
        this.solutionRepository = solutionRepository;
    }

    @Override
    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndAward(Long userId, SolutionSubmittedEvent event) {
        if (event.getRating() != null && event.getRating() == 100L) {
            awardIfNotExists(userId, Achievement.PERFECT_SOLUTION);
        }
        Long solvedCount = solutionRepository.countDistinctCasesSolvedByUserId(userId, SOLVE_THRESHOLD);
        if (solvedCount >= 5) awardIfNotExists(userId, Achievement.RAPID_RISE);

        if (solvedCount >= 20) awardIfNotExists(userId, Achievement.COLLECTOR);

        Long hardSolvedCount = solutionRepository.countDistinctHardCasesSolvedByUserId(userId, SOLVE_THRESHOLD);
        if (hardSolvedCount >= 3) awardIfNotExists(userId, Achievement.HARDCORE_SOLVER);

        Long perfectCasesCount = solutionRepository.countCasesWithMaxRatingByUserId(userId);
        if (perfectCasesCount >= 3) awardIfNotExists(userId, Achievement.PERFECTIONIST);

        if (event.getRating() != null && event.getRating() == 100L) {
            boolean isFirstTry = solutionRepository.isFirstSolutionForCase(userId, event.getCaseId());
            if (isFirstTry) {
                awardIfNotExists(userId, Achievement.FIRST_TRY);
            }
        }
    }

    private void awardIfNotExists(Long userId, Achievement achievement) {
        if (!achievementRepository.existsByUserIdAndAchievementId(userId, achievement.getId())) {
            try {
                achievementRepository.save(new UserAchievement(userId, achievement.getId(), LocalDateTime.now()));
                logger.info("User {} unlocked achievement: {}", userId, achievement.getName());
            } catch (Exception e) {
                logger.debug("Achievement {} already exists for user {} or DB error", achievement.getId(), userId);
            }
        }
    }
}
