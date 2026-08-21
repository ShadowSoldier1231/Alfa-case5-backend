package com.project.main.service.achievement;

import com.project.main.dto.achievement.AchievementDto;
import com.project.main.enums.Achievement;
import com.project.main.repository.user.AchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;

    public AchievementService(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @Transactional(readOnly = true)
    public List<AchievementDto> getAchievementsForUser(Long userId) {
        List<Object[]> obtainedRows = achievementRepository.findObtainedAchievementsByUserId(userId);

        Map<Long, LocalDateTime> obtainedMap = obtainedRows.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> convertToLocalDateTime(row[1])
                ));

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


    private LocalDateTime convertToLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }

}