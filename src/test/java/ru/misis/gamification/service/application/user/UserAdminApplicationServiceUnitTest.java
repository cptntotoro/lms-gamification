package ru.misis.gamification.service.application.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.model.UserAdminView;
import ru.misis.gamification.model.UserProgressView;
import ru.misis.gamification.service.simple.user.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminApplicationServiceUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private UserProgressApplicationService progressApplicationService;

    @InjectMocks
    private UserAdminApplicationServiceImpl service;

    @Test
    void findByUserId_returnsMappedView() {
        String userId = "user-123";
        UUID uuid = UUID.randomUUID();
        User user = User.builder()
                .uuid(uuid)
                .userId(userId)
                .totalPoints(1500)
                .level(8)
                .createdAt(LocalDateTime.now().minusDays(30))
                .updatedAt(LocalDateTime.now().minusHours(2))
                .build();

        UserProgressView progress = new UserProgressView(
                userId,
                1500,
                8,
                200L,
                75.5
        );

        when(userService.getUserByExternalId(userId)).thenReturn(user);
        when(progressApplicationService.getProgress(userId)).thenReturn(progress);

        UserAdminView result = service.findByUserId(userId);

        assertThat(result.uuid()).isEqualTo(uuid);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.totalPoints()).isEqualTo(1500);
        assertThat(result.level()).isEqualTo(8);
        assertThat(result.pointsToNextLevel()).isEqualTo(200L);
        assertThat(result.progressPercent()).isEqualTo(75.5);
        assertThat(result.createdAt()).isEqualTo(user.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(user.getUpdatedAt());

        verify(userService).getUserByExternalId(userId);
        verify(progressApplicationService).getProgress(userId);
    }

    @Test
    void findAll_withCourseAndGroup_mapsPageCorrectly() {
        Pageable pageable = PageRequest.of(0, 10);

        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();

        User user1 = User.builder().uuid(u1).userId("u1").totalPoints(1000).level(5).build();
        User user2 = User.builder().uuid(u2).userId("u2").totalPoints(2000).level(10).build();

        Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, 2);

        UserProgressView p1 = new UserProgressView("u1", 1000, 5, 300L, 60.0);
        UserProgressView p2 = new UserProgressView("u2", 2000, 10, 150L, 90.0);

        when(userService.findAll("MATH-101", "GROUP-A", pageable)).thenReturn(userPage);
        when(progressApplicationService.getProgress("u1")).thenReturn(p1);
        when(progressApplicationService.getProgress("u2")).thenReturn(p2);

        Page<UserAdminView> result = service.findAll("MATH-101", "GROUP-A", pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);

        verify(userService).findAll("MATH-101", "GROUP-A", pageable);
        verify(progressApplicationService).getProgress("u1");
        verify(progressApplicationService).getProgress("u2");
    }

    @Test
    void findAll_withoutFilters_mapsPageCorrectly() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(
                User.builder().uuid(UUID.randomUUID()).userId("u1").totalPoints(1000).level(5).build()
        ));

        UserProgressView progress = new UserProgressView("u1", 1000, 5, 300L, 60.0);

        when(userService.findAll(null, null, pageable)).thenReturn(userPage);
        when(progressApplicationService.getProgress("u1")).thenReturn(progress);

        Page<UserAdminView> result = service.findAll(null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userService).findAll(null, null, pageable);
    }

    @Test
    void findAll_withEmptyStrings_normalizesAndCallsService() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(userService.findAll(null, null, pageable)).thenReturn(userPage);

        Page<UserAdminView> result = service.findAll("", "   ", pageable);

        assertThat(result.isEmpty()).isTrue();

        verify(userService).findAll(null, null, pageable);
    }

    @Test
    void findAll_emptyPage_returnsEmpty() {
        Pageable pageable = PageRequest.of(1, 20);
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(userService.findAll("CS-101", null, pageable)).thenReturn(emptyPage);

        Page<UserAdminView> result = service.findAll("CS-101", null, pageable);

        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getNumber()).isOne();
        assertThat(result.getSize()).isEqualTo(20);

        verify(userService).findAll("CS-101", null, pageable);
        verifyNoInteractions(progressApplicationService);
    }

    @Test
    void findAll_serviceThrowsException_propagates() {
        Pageable pageable = PageRequest.of(0, 5);
        RuntimeException ex = new RuntimeException("DB error");

        when(userService.findAll("ANY-COURSE", "ANY-GROUP", pageable)).thenThrow(ex);

        assertThatThrownBy(() -> service.findAll("ANY-COURSE", "ANY-GROUP", pageable))
                .isSameAs(ex);

        verify(userService).findAll("ANY-COURSE", "ANY-GROUP", pageable);
        verifyNoInteractions(progressApplicationService);
    }
}