package com.project.main.service.achievement;

import com.project.main.dto.achievement.AchievementDto;
import com.project.main.enums.Achievement;
import com.project.main.repository.user.AchievementRepository;
import com.project.main.service.component.TypeMapperComponent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final TypeMapperComponent typeMapper;

    public AchievementService(AchievementRepository achievementRepository,
                              TypeMapperComponent typeMapper) {
        this.achievementRepository = achievementRepository;
        this.typeMapper = typeMapper;
    }

    @Transactional(readOnly = true)
    public List<AchievementDto> getAchievementsForUser(Long userId) {
        List<Object[]> obtainedRows = achievementRepository.findObtainedAchievementsByUserId(userId);


        Map<Long, LocalDateTime> obtainedMap = new HashMap<>();

        for (Object[] row : obtainedRows) {
            if (row[0] == null || row[1] == null) {
                continue;
            }

            Long achievementId = ((Number) row[0]).longValue();
            LocalDateTime obtainedAt = typeMapper.toLocalDateTime(row[1]);

            if (obtainedAt != null) {
                obtainedMap.put(achievementId, obtainedAt);
            }
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