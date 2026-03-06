package ru.misis.gamification.service.application.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.model.UserSummary;
import ru.misis.gamification.service.simple.user.UserService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceUnitTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserApplicationServiceImpl service;

    @Test
    void createIfNotExists_userCreated_returnsSummary() {
        String userId = "user-123";
        String courseId = "CS-101";
        String groupId = "G-1";

        UUID uuid = UUID.randomUUID();
        User created = User.builder()
                .uuid(uuid)
                .userId(userId)
                .totalPoints(0)
                .level(1)
                .build();

        when(userService.createIfNotExists(userId, courseId, groupId)).thenReturn(created);

        UserSummary result = service.createIfNotExists(userId, courseId, groupId);

        assertThat(result.uuid()).isEqualTo(uuid);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.totalPoints()).isZero();
        assertThat(result.level()).isEqualTo(1);

        verify(userService).createIfNotExists(userId, courseId, groupId);
    }

    @Test
    void createIfNotExists_userAlreadyExists_returnsSummary() {
        String userId = "existing-user";
        String courseId = "MATH-202";
        String groupId = null;

        UUID uuid = UUID.randomUUID();
        User existing = User.builder()
                .uuid(uuid)
                .userId(userId)
                .totalPoints(500)
                .level(3)
                .build();

        when(userService.createIfNotExists(userId, courseId, groupId)).thenReturn(existing);

        UserSummary result = service.createIfNotExists(userId, courseId, groupId);

        assertThat(result.totalPoints()).isEqualTo(500);
        assertThat(result.level()).isEqualTo(3);

        verify(userService).createIfNotExists(userId, courseId, groupId);
    }

    @Test
    void createIfNotExists_nullCourseAndGroup_callsServiceCorrectly() {
        String userId = "user-nulls";

        User user = User.builder().uuid(UUID.randomUUID()).userId(userId).build();

        when(userService.createIfNotExists(userId, null, null)).thenReturn(user);

        service.createIfNotExists(userId, null, null);

        verify(userService).createIfNotExists(userId, null, null);
    }

    @Test
    void getUserSummary_returnsMappedSummary() {
        String userId = "user-456";
        UUID uuid = UUID.randomUUID();

        User user = User.builder()
                .uuid(uuid)
                .userId(userId)
                .totalPoints(1200)
                .level(6)
                .build();

        when(userService.getUserByExternalId(userId)).thenReturn(user);

        UserSummary result = service.getUserSummary(userId);

        assertThat(result.uuid()).isEqualTo(uuid);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.totalPoints()).isEqualTo(1200);
        assertThat(result.level()).isEqualTo(6);

        verify(userService).getUserByExternalId(userId);
    }

    @Test
    void getUserSummary_serviceThrowsException_propagates() {
        String userId = "missing-user";
        RuntimeException ex = new RuntimeException("User not found");

        when(userService.getUserByExternalId(userId)).thenThrow(ex);

        assertThatThrownBy(() -> service.getUserSummary(userId))
                .isSameAs(ex);

        verify(userService).getUserByExternalId(userId);
    }

    @Test
    void createIfNotExists_serviceThrowsException_propagates() {
        String userId = "error-user";
        RuntimeException ex = new RuntimeException("Creation failed");

        when(userService.createIfNotExists(userId, "CS-101", "G-1")).thenThrow(ex);

        assertThatThrownBy(() -> service.createIfNotExists(userId, "CS-101", "G-1"))
                .isSameAs(ex);

        verify(userService).createIfNotExists(userId, "CS-101", "G-1");
    }
}