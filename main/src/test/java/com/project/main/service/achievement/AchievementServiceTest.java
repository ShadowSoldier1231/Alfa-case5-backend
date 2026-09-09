package com.project.main.service.achievement;

import com.project.main.dto.achievement.AchievementDto;
import com.project.main.enums.Achievement;
import com.project.main.repository.projection.ObtainedAchievementRow;
import com.project.main.repository.user.AchievementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock private AchievementRepository achievementRepository;

    private AchievementService service() {
        return new AchievementService(achievementRepository);
    }

    @Test
    void returnsAllAchievementsLockedWhenUserHasNone() {
        when(achievementRepository.findObtainedAchievementsByUserId(1L)).thenReturn(List.of());

        List<AchievementDto> dtos = service().getAchievementsForUser(1L);

        assertThat(dtos).hasSize(Achievement.values().length);
        assertThat(dtos).allMatch(dto -> dto.obtainedAt() == null);
    }

    @Test
    void marksObtainedAchievementWithDate() {
        LocalDateTime obtainedAt = LocalDateTime.of(2026, 1, 10, 12, 0);
        ObtainedAchievementRow row = mock(ObtainedAchievementRow.class);
        when(row.getAchievement_id()).thenReturn(Achievement.RAPID_RISE.getId());
        when(row.getObtained_at()).thenReturn(obtainedAt);
        when(achievementRepository.findObtainedAchievementsByUserId(1L)).thenReturn(List.of(row));

        List<AchievementDto> dtos = service().getAchievementsForUser(1L);

        assertThat(dtos)
                .filteredOn(dto -> dto.id().equals(Achievement.RAPID_RISE.getId()))
                .singleElement()
                .satisfies(dto -> assertThat(dto.obtainedAt()).isEqualTo(obtainedAt));
        assertThat(dtos)
                .filteredOn(dto -> !dto.id().equals(Achievement.RAPID_RISE.getId()))
                .allMatch(dto -> dto.obtainedAt() == null);
    }
}