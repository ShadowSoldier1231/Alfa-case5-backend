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
public class TagAchievementChecker implements AchievementChecker {

    private static final Logger logger = LoggerFactory.getLogger(TagAchievementChecker.class);
    private final AchievementRepository achievementRepository;
    private final SolutionRepository solutionRepository;

    private static final Long SOLVE_THRESHOLD = 70L;

    public TagAchievementChecker(AchievementRepository achievementRepository, SolutionRepository solutionRepository) {
        this.achievementRepository = achievementRepository;
        this.solutionRepository = solutionRepository;
    }

    @Override
    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndAward(Long userId, SolutionSubmittedEvent event) {
        Long dataScienceCount = solutionRepository.countSolvedCasesByTagName(userId, "Data Science", SOLVE_THRESHOLD);
        if (dataScienceCount >= 5) awardIfNotExists(userId, Achievement.DATA_MASTER);

        Long businessCount = solutionRepository.countSolvedCasesByTagName(userId, "Бизнес-стратегия", SOLVE_THRESHOLD);
        if (businessCount >= 5) awardIfNotExists(userId, Achievement.BUSINESS_LEADER);

        Long uniqueTagsCount = solutionRepository.countUniqueTagsInSolvedCases(userId, SOLVE_THRESHOLD);
        if (uniqueTagsCount >= 5) awardIfNotExists(userId, Achievement.TAG_MASTER);
    }

    private void awardIfNotExists(Long userId, Achievement achievement) {
        if (!achievementRepository.existsByUserIdAndAchievementId(userId, achievement.getId())) {
            try {
                achievementRepository.save(new UserAchievement(userId, achievement.getId(), LocalDateTime.now()));
                logger.info("User {} unlocked achievement: {}", userId, achievement.getName());
            } catch (Exception e) {
                logger.debug("Achievement {} already exists for user {}", achievement.getId(), userId);
            }
        }
    }
}