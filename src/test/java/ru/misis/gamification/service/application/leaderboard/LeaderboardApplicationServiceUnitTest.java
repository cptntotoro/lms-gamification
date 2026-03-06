package ru.misis.gamification.service.application.leaderboard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.CourseNotFoundException;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.model.LeaderboardEntryView;
import ru.misis.gamification.model.LeaderboardPageView;
import ru.misis.gamification.model.UserCourseGroupLeaderboardView;
import ru.misis.gamification.service.simple.course.CourseService;
import ru.misis.gamification.service.simple.enrollment.EnrollmentService;
import ru.misis.gamification.service.simple.group.GroupService;
import ru.misis.gamification.service.simple.user.UserService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardApplicationServiceUnitTest {

    @Mock
    private CourseService courseService;

    @Mock
    private GroupService groupService;

    @Mock
    private UserService userService;

    @Mock
    private EnrollmentService enrollmentService;

    @InjectMocks
    private LeaderboardApplicationServiceImpl service;

    @Test
    void getGroupLeaderboard_courseOnly_returnsPage() {
        UUID courseUuid = UUID.randomUUID();
        when(courseService.getCourseUuidByExternalId("CS-101")).thenReturn(courseUuid);

        LeaderboardEntryView entry = new LeaderboardEntryView(UUID.randomUUID(), "u1", 500, 5, 1L, false);
        Page<LeaderboardEntryView> page = new PageImpl<>(List.of(entry), PageRequest.of(0, 10), 1);
        when(enrollmentService.findLeaderboardByCourseAndGroup(eq(courseUuid), isNull(), any(Pageable.class))).thenReturn(page);

        LeaderboardPageView result = service.getGroupLeaderboard("CS-101", null, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.pageNumber()).isZero();
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.totalElements()).isOne();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.hasPrevious()).isFalse();
    }

    @Test
    void getGroupLeaderboard_withGroup_returnsPage() {
        UUID courseUuid = UUID.randomUUID();
        UUID groupUuid = UUID.randomUUID();
        when(courseService.getCourseUuidByExternalId("CS-101")).thenReturn(courseUuid);
        when(groupService.getGroupUuidByExternalIdAndCourseId("G-1", "CS-101")).thenReturn(groupUuid);

        Page<LeaderboardEntryView> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 20), 0);
        when(enrollmentService.findLeaderboardByCourseAndGroup(eq(courseUuid), eq(groupUuid), any(Pageable.class))).thenReturn(page);

        LeaderboardPageView result = service.getGroupLeaderboard("CS-101", "G-1", 1, 20);

        assertThat(result.content()).isEmpty();
        assertThat(result.pageNumber()).isOne();
        assertThat(result.pageSize()).isEqualTo(20);
    }

    @Test
    void getCourseLeaderboardForUser_userEnrolled_returnsWithCurrentUser() {
        UUID courseUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        when(courseService.getCourseUuidByExternalId("CS-101")).thenReturn(courseUuid);
        when(userService.getUserByExternalId("u-123")).thenReturn(User.builder().uuid(userUuid).userId("u-123").level(4).build());

        Course course = Course.builder().uuid(courseUuid).build();
        when(courseService.findByCourseId("CS-101")).thenReturn(course);

        when(enrollmentService.isUserEnrolledInCourse(any(), eq(course))).thenReturn(true);

        UserCourseEnrollment enrollment = UserCourseEnrollment.builder().totalPointsInCourse(750).build();
        when(enrollmentService.findByUserAndCourse(any(), eq(course))).thenReturn(enrollment);

        when(enrollmentService.getRankByPointsInCourse(eq(courseUuid), isNull(), eq(userUuid))).thenReturn(3L);

        LeaderboardEntryView top1 = new LeaderboardEntryView(UUID.randomUUID(), "top1", 1200, 6, 1L, false);
        Page<LeaderboardEntryView> topPage = new PageImpl<>(List.of(top1), PageRequest.of(0, 5), 10);
        when(enrollmentService.findLeaderboardByCourseAndGroup(eq(courseUuid), isNull(), any(Pageable.class))).thenReturn(topPage);

        UserCourseGroupLeaderboardView result = service.getCourseLeaderboardForUser("CS-101", null, 0, 5, "u-123");

        assertThat(result.topEntries()).hasSize(1);
        assertThat(result.currentUserEntry().getIsCurrentUser()).isTrue();
        assertThat(result.currentUserRank()).isEqualTo(3L);
        assertThat(result.currentUserPoints()).isEqualTo(750);
    }

    @Test
    void getCourseLeaderboardForUser_userNotEnrolled_currentUserNull() {
        UUID courseUuid = UUID.randomUUID();
        when(courseService.getCourseUuidByExternalId("CS-101")).thenReturn(courseUuid);
        when(userService.getUserByExternalId("u-999")).thenReturn(User.builder().uuid(UUID.randomUUID()).build());

        Course course = Course.builder().uuid(courseUuid).build();
        when(courseService.findByCourseId("CS-101")).thenReturn(course);

        when(enrollmentService.isUserEnrolledInCourse(any(), eq(course))).thenReturn(false);

        Page<LeaderboardEntryView> page = new PageImpl<>(Collections.emptyList());
        when(enrollmentService.findLeaderboardByCourseAndGroup(any(), isNull(), any())).thenReturn(page);

        UserCourseGroupLeaderboardView result = service.getCourseLeaderboardForUser("CS-101", null, 0, 10, "u-999");

        assertThat(result.currentUserEntry()).isNull();
        assertThat(result.currentUserRank()).isNull();
        assertThat(result.currentUserPoints()).isNull();
    }

    @Test
    void getCourseLeaderboardForUser_userNotFound_currentUserNull() {
        when(userService.getUserByExternalId("u-missing")).thenThrow(new UserNotFoundException("missing"));

        Page<LeaderboardEntryView> emptyPage = new PageImpl<>(Collections.emptyList());
        when(enrollmentService.findLeaderboardByCourseAndGroup(any(), any(), any())).thenReturn(emptyPage);

        UserCourseGroupLeaderboardView result = service.getCourseLeaderboardForUser("CS-101", null, 0, 10, "u-missing");

        assertThat(result.currentUserEntry()).isNull();
        assertThat(result.currentUserRank()).isNull();
        assertThat(result.currentUserPoints()).isNull();
        assertThat(result.topEntries()).isEmpty();
    }

    @Test
    void getCourseLeaderboardForUser_courseNotFound_currentUserNull() {
        UUID courseUuid = UUID.randomUUID();
        when(courseService.getCourseUuidByExternalId("CS-101")).thenReturn(courseUuid);
        when(userService.getUserByExternalId("u-123")).thenReturn(User.builder().uuid(UUID.randomUUID()).build());
        when(courseService.findByCourseId("CS-101")).thenThrow(new CourseNotFoundException("missing"));

        Page<LeaderboardEntryView> emptyPage = new PageImpl<>(Collections.emptyList());
        when(enrollmentService.findLeaderboardByCourseAndGroup(any(), any(), any())).thenReturn(emptyPage);

        UserCourseGroupLeaderboardView result = service.getCourseLeaderboardForUser("CS-101", null, 0, 10, "u-123");

        assertThat(result.currentUserEntry()).isNull();
        assertThat(result.currentUserRank()).isNull();
        assertThat(result.currentUserPoints()).isNull();
        assertThat(result.topEntries()).isEmpty();
    }

    @Test
    void getGroupLeaderboard_groupNotFound_throws() {
        when(courseService.getCourseUuidByExternalId("CS-101")).thenReturn(UUID.randomUUID());
        when(groupService.getGroupUuidByExternalIdAndCourseId("G-missing", "CS-101"))
                .thenThrow(new RuntimeException("group not found"));

        assertThatThrownBy(() -> service.getGroupLeaderboard("CS-101", "G-missing", 0, 10))
                .isInstanceOf(RuntimeException.class);
    }
}