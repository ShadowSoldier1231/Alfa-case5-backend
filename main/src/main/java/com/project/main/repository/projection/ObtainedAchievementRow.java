package com.project.main.repository.projection;

import java.time.LocalDateTime;

public interface ObtainedAchievementRow {
    Long getAchievement_id();
    LocalDateTime getObtained_at();
}