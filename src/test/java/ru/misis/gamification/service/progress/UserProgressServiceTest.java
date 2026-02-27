package ru.misis.gamification.service.progress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.dto.web.response.UserDto;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.service.progress.calculator.ProgressCalculator;
import ru.misis.gamification.service.progress.result.ProgressMetrics;
import ru.misis.gamification.service.user.UserService;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProgressServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ProgressCalculator progressCalculator;

    @InjectMocks
    private UserProgressServiceImpl userProgressService;

    private User user;
    private ProgressMetrics metrics;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .uuid(UUID.randomUUID())
                .userId("test-user-001")
                .totalPoints(1250)
                .level(7)
                .createdAt(LocalDateTime.of(2026, 1, 15, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 2, 20, 14, 30))
                .build();

        metrics = new ProgressMetrics(1750L, 71.42857142857143);
    }

    @Test
    void getProgress_returnsCorrectUserDto() {
        when(userService.get("test-user-001")).thenReturn(user);
        when(progressCalculator.calculate(user)).thenReturn(metrics);

        UserDto dto = userProgressService.getProgress("test-user-001");

        assertThat(dto.getUserId()).isEqualTo("test-user-001");
        assertThat(dto.getTotalPoints()).isEqualTo(1250);
        assertThat(dto.getLevel()).isEqualTo(7);
        assertThat(dto.getPointsToNextLevel()).isEqualTo(1750L);
        assertThat(dto.getProgressPercent()).isEqualTo(71.42857142857143);

        verify(userService).get("test-user-001");
        verify(progressCalculator).calculate(user);
    }

    @Test
    void getProgress_whenUserNotFound_throwsUserNotFoundException() {
        when(userService.get("unknown")).thenThrow(new UserNotFoundException("unknown"));

        assertThatThrownBy(() -> userProgressService.getProgress("unknown"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getAdminProgress_returnsCorrectUserAdminDto() {
        when(userService.get("test-user-001")).thenReturn(user);
        when(progressCalculator.calculate(user)).thenReturn(metrics);

        UserAdminDto dto = userProgressService.getAdminProgress("test-user-001");

        assertThat(dto.getUuid()).isEqualTo(user.getUuid());
        assertThat(dto.getUserId()).isEqualTo("test-user-001");
        assertThat(dto.getTotalPoints()).isEqualTo(1250);
        assertThat(dto.getLevel()).isEqualTo(7);
        assertThat(dto.getCreatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(dto.getUpdatedAt()).isEqualTo(user.getUpdatedAt());
        assertThat(dto.getPointsToNextLevel()).isEqualTo(1750L);
        assertThat(dto.getProgressPercent()).isEqualTo(71.42857142857143);

        verify(userService).get("test-user-001");
        verify(progressCalculator).calculate(user);
    }

    @Test
    void getAdminProgress_whenUserNotFound_throwsUserNotFoundException() {
        when(userService.get("unknown")).thenThrow(new UserNotFoundException("unknown"));

        assertThatThrownBy(() -> userProgressService.getAdminProgress("unknown"))
                .isInstanceOf(UserNotFoundException.class);
    }
}