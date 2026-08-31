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

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Component
public class TimeAndSpeedChecker implements AchievementChecker {

    private static final Logger logger = LoggerFactory.getLogger(TimeAndSpeedChecker.class);
    private final AchievementRepository achievementRepository;
    private final SolutionRepository solutionRepository;

    private static final Long SOLVE_THRESHOLD = 70L;

    public TimeAndSpeedChecker(AchievementRepository achievementRepository, SolutionRepository solutionRepository) {
        this.achievementRepository = achievementRepository;
        this.solutionRepository = solutionRepository;
    }

    @Override
    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndAward(Long userId, SolutionSubmittedEvent event) {

        if (event.getTimeSpentMinutes() != null && event.getTimeSpentMinutes() < 30) {
            if (solutionRepository.existsFirstSolutionUnder30Min(userId, SOLVE_THRESHOLD)) {
                awardIfNotExists(userId, Achievement.QUICK_START);
            }
        }

        if (solutionRepository.existsFasterThanAverageSolution(userId, SOLVE_THRESHOLD)) {
            awardIfNotExists(userId, Achievement.SPRINTER);
        }

        checkMarathoner(userId);
    }

    private void checkMarathoner(Long userId) {
        List<Object> rows = solutionRepository.findDistinctSolveDatesByUserId(userId, SOLVE_THRESHOLD);

        if (rows.size() >= 3) {
            int consecutiveDays = 1;
            LocalDate prevDate = null;

            for (int i = 0; i < rows.size(); i++) {
                LocalDate currDate = toLocalDate(rows.get(i));

                if (currDate == null) {
                    consecutiveDays = 1;
                    prevDate = null;
                    continue;
                }

                if (prevDate == null) {
                    prevDate = currDate;
                    continue;
                }

                if (prevDate.minusDays(1).equals(currDate)) {
                    consecutiveDays++;
                    if (consecutiveDays >= 3) {
                        awardIfNotExists(userId, Achievement.MARATHONER);
                        return;
                    }
                } else {
                    consecutiveDays = 1;
                }
                prevDate = currDate;
            }
        }
    }

    private void awardIfNotExists(Long userId, Achievement achievement) {
        if (!achievementRepository.existsByUserIdAndAchievementId(userId, achievement.getId())) {
            try {
                achievementRepository.save(new UserAchievement(userId, achievement.getId()));
                logger.info("User {} unlocked achievement: {}", userId, achievement.getName());
            } catch (Exception e) {
                logger.debug("Achievement {} already exists for user {}", achievement.getId(), userId);
            }
        }
    }


    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        if (value instanceof java.util.Date utilDate) {
            return utilDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        }

        logger.warn(
                "Unsupported date value for marathoner achievement: value='{}', class='{}'",
                value,
                value.getClass().getName()
        );

        return null;
    }
}