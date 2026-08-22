package com.project.main.service.achievement;

import com.project.main.dto.achievement.AchievementDto;
import com.project.main.enums.Achievement;
import com.project.main.repository.user.AchievementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private static final Logger logger = LoggerFactory.getLogger(AchievementService.class);

    public AchievementService(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @Transactional(readOnly = true)
    public List<AchievementDto> getAchievementsForUser(Long userId) {
        List<Object[]> obtainedRows = achievementRepository.findObtainedAchievementsByUserId(userId);

        logger.info("Found {} obtained achievements for user {}", obtainedRows.size(), userId);
        for (Object[] row : obtainedRows) {
            logger.info("Raw DB row -> achievement_id: {}, obtained_at: {}, class: {}",
                    row[0], row[1], row[1] != null ? row[1].getClass().getName() : "null");
        }

        Map<Long, LocalDateTime> obtainedMap = obtainedRows.stream()
                .filter(row -> row[1] != null)
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
        if (value instanceof OffsetDateTime odt) return odt.toLocalDateTime();
        if (value instanceof Instant instant) return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (value instanceof Date date) return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();


        logger.warn("Unexpected date type from DB: value='{}', class='{}'", value, value.getClass().getName());
        return null;
    }

}