package com.project.main.service.achievement;

import com.project.main.dto.event.SolutionSubmittedEvent;

public interface AchievementChecker {
    void checkAndAward(Long userId, SolutionSubmittedEvent event);
}