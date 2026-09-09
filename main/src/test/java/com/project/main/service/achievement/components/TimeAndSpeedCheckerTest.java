package com.project.main.service.achievement.components;

import com.project.main.dto.event.SolutionSubmittedEvent;
import com.project.main.enums.Achievement;
import com.project.main.model.user.UserAchievement;
import com.project.main.repository.cases.SolutionRepository;
import com.project.main.repository.projection.SolveDateRow;
import com.project.main.repository.user.AchievementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeAndSpeedCheckerTest {

    private static final Long USER_ID = 1L;
    private static final Long THRESHOLD = 70L;

    @Mock private AchievementRepository achievementRepository;
    @Mock private SolutionRepository solutionRepository;

    private TimeAndSpeedChecker checker() {
        return new TimeAndSpeedChecker(achievementRepository, solutionRepository);
    }

    private SolveDateRow row(LocalDate date) {
        SolveDateRow row = mock(SolveDateRow.class);
        when(row.getSolve_date()).thenReturn(date);
        return row;
    }

    @Test
    void quickStartIsAwardedWhenFirstSolutionTookLessThan30Minutes() {
        SolutionSubmittedEvent event = new SolutionSubmittedEvent(USER_ID, 4L, 80L, 25);
        when(solutionRepository.existsFirstSolutionUnder30Min(USER_ID, THRESHOLD)).thenReturn(true);
        when(solutionRepository.existsFasterThanAverageSolution(USER_ID, THRESHOLD)).thenReturn(false);
        when(solutionRepository.findDistinctSolveDatesByUserId(USER_ID, THRESHOLD)).thenReturn(List.of());
        when(achievementRepository.existsByUserIdAndAchievementId(USER_ID, Achievement.QUICK_START.getId()))
                .thenReturn(false);

        checker().checkAndAward(USER_ID, event);

        ArgumentCaptor<UserAchievement> captor = ArgumentCaptor.forClass(UserAchievement.class);
        verify(achievementRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getAchievementId()).isEqualTo(Achievement.QUICK_START.getId());
    }

    @Test
    void marathonerIsAwardedForThreeConsecutiveDays() {
        SolutionSubmittedEvent event = new SolutionSubmittedEvent(USER_ID, 4L, 80L, null);
        when(solutionRepository.existsFasterThanAverageSolution(USER_ID, THRESHOLD)).thenReturn(false);
        when(solutionRepository.findDistinctSolveDatesByUserId(USER_ID, THRESHOLD)).thenReturn(List.of(
                row(LocalDate.of(2026, 1, 3)),
                row(LocalDate.of(2026, 1, 2)),
                row(LocalDate.of(2026, 1, 1)))); // запрос отдаёт даты по убыванию
        when(achievementRepository.existsByUserIdAndAchievementId(USER_ID, Achievement.MARATHONER.getId()))
                .thenReturn(false);

        checker().checkAndAward(USER_ID, event);

        ArgumentCaptor<UserAchievement> captor = ArgumentCaptor.forClass(UserAchievement.class);
        verify(achievementRepository).save(captor.capture());
        assertThat(captor.getValue().getAchievementId()).isEqualTo(Achievement.MARATHONER.getId());
    }

    @Test
    void nothingIsAwardedWhenSolveDaysHaveGaps() {
        SolutionSubmittedEvent event = new SolutionSubmittedEvent(USER_ID, 4L, 80L, null);
        when(solutionRepository.existsFasterThanAverageSolution(USER_ID, THRESHOLD)).thenReturn(false);
        when(solutionRepository.findDistinctSolveDatesByUserId(USER_ID, THRESHOLD)).thenReturn(List.of(
                row(LocalDate.of(2026, 1, 5)),
                row(LocalDate.of(2026, 1, 3)),
                row(LocalDate.of(2026, 1, 1)))); // разрывы дней

        checker().checkAndAward(USER_ID, event);

        verify(achievementRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}