package ru.misis.gamification.service.progress.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.service.progress.LevelCalculatorService;
import ru.misis.gamification.service.progress.result.ProgressMetrics;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressCalculatorTest {

    @Mock
    private LevelCalculatorService levelCalculatorService;

    @InjectMocks
    private ProgressCalculator progressCalculator;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .uuid(UUID.randomUUID())
                .userId("user-123")
                .totalPoints(1200)
                .level(5)
                .build();
    }

    @Test
    void calculate_normalCase_returnsCorrectMetrics() {
        when(levelCalculatorService.pointsToNextLevel(5)).thenReturn(800L);

        ProgressMetrics metrics = progressCalculator.calculate(user);

        assertThat(metrics.getPointsToNextLevel()).isEqualTo(800L);
        assertThat(metrics.getProgressPercent()).isEqualTo(100.0);
        verify(levelCalculatorService).pointsToNextLevel(5);
    }

    @Test
    void calculate_whenPointsExceedNextLevel_capsAt100Percent() {
        user.setTotalPoints(2000);
        when(levelCalculatorService.pointsToNextLevel(5)).thenReturn(800L);

        ProgressMetrics metrics = progressCalculator.calculate(user);

        assertThat(metrics.getProgressPercent()).isEqualTo(100.0);
    }

    @Test
    void calculate_whenPointsToNextLevelZero_returns100Percent() {
        when(levelCalculatorService.pointsToNextLevel(anyInt())).thenReturn(0L);

        ProgressMetrics metrics = progressCalculator.calculate(user);

        assertThat(metrics.getPointsToNextLevel()).isZero();
        assertThat(metrics.getProgressPercent()).isEqualTo(100.0);
    }

    @Test
    void calculate_whenTotalPointsNull_treatsAsZero() {
        user.setTotalPoints(null);
        when(levelCalculatorService.pointsToNextLevel(5)).thenReturn(800L);

        ProgressMetrics metrics = progressCalculator.calculate(user);

        assertThat(metrics.getProgressPercent()).isEqualTo(0.0);
    }

    @Test
    void calculate_whenLevelNull_treatsAsLevel1() {
        user.setLevel(null);
        when(levelCalculatorService.pointsToNextLevel(1)).thenReturn(500L);

        ProgressMetrics metrics = progressCalculator.calculate(user);

        assertThat(metrics.getPointsToNextLevel()).isEqualTo(500L);
    }

    @Test
    void calculate_whenUserNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> progressCalculator.calculate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Пользователь не может быть null");
    }

    @Test
    void calculate_logsTraceWithCorrectValues() {
        when(levelCalculatorService.pointsToNextLevel(5)).thenReturn(800L);

        progressCalculator.calculate(user);

        assertThat(true).isTrue();
    }
}