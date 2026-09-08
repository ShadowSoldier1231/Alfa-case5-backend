package com.project.main.service.achievement;

import com.project.main.dto.achievement.AchievementDto;
import com.project.main.enums.Achievement;
import com.project.main.repository.projection.ObtainedAchievementRow;
import com.project.main.repository.user.AchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;

    public AchievementService(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @Transactional(readOnly = true)
    public List<AchievementDto> getAchievementsForUser(Long userId) {
        List<ObtainedAchievementRow> obtainedRows =
                achievementRepository.findObtainedAchievementsByUserId(userId);

        Map<Long, LocalDateTime> obtainedMap = new HashMap<>();

        for (ObtainedAchievementRow row : obtainedRows) {
            if (row.getAchievement_id() == null || row.getObtained_at() == null) {
                continue;
            }

            obtainedMap.put(row.getAchievement_id(), row.getObtained_at());
        }

        return Arrays.stream(Achievement.values())
                .map(ach -> {
                    LocalDateTime obtainedAt = obtainedMap.get(ach.getId());

                    return new AchievementDto(
                            ach.getId(),
                            ach.getName(),
                            ach.getDescription(),
                            ach.getIconUrl(),
                            obtainedAt
                    );
                })
                .toList();
    }
}