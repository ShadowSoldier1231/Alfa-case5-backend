package com.project.main.service.achievement;


import com.project.main.dto.event.SolutionSubmittedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import java.util.List;

@Service
public class AchievementEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AchievementEventListener.class);
    private final List<AchievementChecker> checkers;

    public AchievementEventListener(List<AchievementChecker> checkers) {
        this.checkers = checkers;
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
}