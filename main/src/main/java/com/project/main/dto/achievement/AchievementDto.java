package com.project.main.dto.achievement;

import java.time.LocalDateTime;



public record AchievementDto(
        Long id,
        String name,
        String description,
        String iconUrl,
        LocalDateTime obtainedAt
) {}