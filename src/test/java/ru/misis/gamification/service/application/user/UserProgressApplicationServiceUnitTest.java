package ru.misis.gamification.service.application.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.model.UserProgressView;
import ru.misis.gamification.service.simple.progress.LevelCalculatorService;
import ru.misis.gamification.service.simple.user.UserService;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProgressApplicationServiceUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private LevelCalculatorService levelCalculator;

    @InjectMocks
    private UserProgressApplicationServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        // Задаём значения полям через reflection (один раз перед всеми тестами)
        Field defaultPointsField = UserProgressApplicationServiceImpl.class.getDeclaredField("defaultPoints");
        defaultPointsField.setAccessible(true);
        defaultPointsField.set(service, 0);

        Field defaultLevelField = UserProgressApplicationServiceImpl.class.getDeclaredField("defaultLevel");
        defaultLevelField.setAccessible(true);
        defaultLevelField.set(service, 1);

        Field maxProgressField = UserProgressApplicationServiceImpl.class.getDeclaredField("maxProgress");
        maxProgressField.setAccessible(true);
        maxProgressField.set(service, 100.0);
    }

    @Test
    void getProgress_userExists_returnsCorrectView() {
        String userId = "user-123";
        User user = User.builder()
                .userId(userId)
                .totalPoints(1500)
                .level(8)
                .build();

        when(userService.getUserByExternalId(userId)).thenReturn(user);
        when(levelCalculator.pointsToNextLevel(8)).thenReturn(500L);

        UserProgressView result = service.getProgress(userId);

        assertThat(result.userId()).isEqualTo("user-123");
        assertThat(result.totalPoints()).isEqualTo(1500);
        assertThat(result.level()).isEqualTo(8);
        assertThat(result.pointsToNextLevel()).isEqualTo(500L);
        assertThat(result.progressPercent()).isEqualTo(100.0); // обрезано min(300, 100)

        verify(userService).getUserByExternalId(userId);
        verify(levelCalculator).pointsToNextLevel(8);
    }

    @Test
    void getProgress_userHasNullPointsAndLevel_usesDefaults() {
        String userId = "user-nulls";
        User user = User.builder()
                .userId(userId)
                .totalPoints(null)
                .level(null)
                .build();

        when(userService.getUserByExternalId(userId)).thenReturn(user);

        // lenient для любого level, чтобы не падало на strict
        lenient().when(levelCalculator.pointsToNextLevel(anyInt())).thenReturn(100L);

        UserProgressView result = service.getProgress(userId);

        assertThat(result.totalPoints()).isZero();
        assertThat(result.level()).isEqualTo(1);
        assertThat(result.pointsToNextLevel()).isEqualTo(100L);
        assertThat(result.progressPercent()).isEqualTo(0.0);

        verify(levelCalculator).pointsToNextLevel(1);
    }

    @Test
    void getProgress_pointsToNextZero_progressIs100() {
        String userId = "user-max";
        User user = User.builder()
                .userId(userId)
                .totalPoints(5000)
                .level(10)
                .build();

        when(userService.getUserByExternalId(userId)).thenReturn(user);
        when(levelCalculator.pointsToNextLevel(10)).thenReturn(0L);

        UserProgressView result = service.getProgress(userId);

        assertThat(result.progressPercent()).isEqualTo(100.0);

        verify(levelCalculator).pointsToNextLevel(10);
    }

    @Test
    void getProgress_pointsLessThanNext_calculatesCorrectPercent() {
        String userId = "user-partial";
        User user = User.builder()
                .userId(userId)
                .totalPoints(120)
                .level(2)
                .build();

        when(userService.getUserByExternalId(userId)).thenReturn(user);
        when(levelCalculator.pointsToNextLevel(2)).thenReturn(300L);

        UserProgressView result = service.getProgress(userId);

        assertThat(result.progressPercent()).isEqualTo(40.0);

        verify(levelCalculator).pointsToNextLevel(2);
    }

    @Test
    void getProgress_serviceThrowsException_propagates() {
        String userId = "missing-user";
        RuntimeException ex = new RuntimeException("User not found");

        when(userService.getUserByExternalId(userId)).thenThrow(ex);

        assertThatThrownBy(() -> service.getProgress(userId))
                .isSameAs(ex);

        verify(userService).getUserByExternalId(userId);
        verifyNoInteractions(levelCalculator);
    }
}