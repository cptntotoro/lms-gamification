package ru.misis.gamification.service.application.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.UserNotEnrolledInCourseException;
import ru.misis.gamification.model.UserProgressView;
import ru.misis.gamification.model.UserStatisticsView;
import ru.misis.gamification.service.simple.course.CourseService;
import ru.misis.gamification.service.simple.enrollment.EnrollmentService;
import ru.misis.gamification.service.simple.group.GroupService;
import ru.misis.gamification.service.simple.user.UserService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStatisticsApplicationServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private CourseService courseService;
    @Mock
    private GroupService groupService;
    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private UserProgressApplicationService progressApplicationService;

    @InjectMocks
    private UserStatisticsApplicationServiceImpl service;

    @Test
    void getUserStatistics_withGroup_returnsFullView() {
        String userId = "u-123";
        String courseId = "CS-101";
        String groupId = "G-1";

        UUID userUuid = UUID.randomUUID();
        UUID courseUuid = UUID.randomUUID();
        UUID groupUuid = UUID.randomUUID();

        User user = User.builder().uuid(userUuid).userId(userId).totalPoints(1200).level(6).build();
        Course course = Course.builder().uuid(courseUuid).build();

        UserCourseEnrollment enrollment = UserCourseEnrollment.builder()
                .totalPointsInCourse(450)
                .build();

        UserProgressView progress = new UserProgressView(userId, 1200, 6, 300L, 80.0);

        when(userService.getUserByExternalId(userId)).thenReturn(user);
        when(courseService.findByCourseId(courseId)).thenReturn(course);
        when(groupService.getGroupUuidByExternalIdAndCourseId(groupId, courseId)).thenReturn(groupUuid);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(true);
        when(enrollmentService.findByUserAndCourse(user, course)).thenReturn(enrollment);
        when(enrollmentService.getRankByPointsInCourse(courseUuid, null, userUuid)).thenReturn(5L);
        when(enrollmentService.getRankByPointsInCourse(courseUuid, groupUuid, userUuid)).thenReturn(2L);
        when(progressApplicationService.getProgress(userId)).thenReturn(progress);

        UserStatisticsView result = service.getUserStatistics(courseId, groupId, userId);

        assertThat(result.userId()).isEqualTo("u-123");
        assertThat(result.globalLevel()).isEqualTo(6);
        assertThat(result.totalPoints()).isEqualTo(1200);
        assertThat(result.courseId()).isEqualTo("CS-101");
        assertThat(result.groupId()).isEqualTo("G-1");
        assertThat(result.pointsInCourse()).isEqualTo(450);
        assertThat(result.rankInCourse()).isEqualTo(5L);
        assertThat(result.rankInGroup()).isEqualTo(2L);
        assertThat(result.pointsToNextGlobalLevel()).isEqualTo(300L);
        assertThat(result.progressPercent()).isEqualTo(80.0);

        verify(enrollmentService).isUserEnrolledInCourse(user, course);
        verify(enrollmentService).getRankByPointsInCourse(courseUuid, null, userUuid);
        verify(enrollmentService).getRankByPointsInCourse(courseUuid, groupUuid, userUuid);
    }

    @Test
    void getUserStatistics_withoutGroup_rankInGroupNull() {
        String userId = "u-456";
        String courseId = "MATH-202";
        String groupId = null;

        UUID userUuid = UUID.randomUUID();
        UUID courseUuid = UUID.randomUUID();

        User user = User.builder().uuid(userUuid).userId(userId).build();
        Course course = Course.builder().uuid(courseUuid).build();

        UserCourseEnrollment enrollment = UserCourseEnrollment.builder().totalPointsInCourse(800).build();

        UserProgressView progress = new UserProgressView(userId, 2000, 9, 400L, 66.6);

        when(userService.getUserByExternalId(userId)).thenReturn(user);
        when(courseService.findByCourseId(courseId)).thenReturn(course);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(true);
        when(enrollmentService.findByUserAndCourse(user, course)).thenReturn(enrollment);
        when(enrollmentService.getRankByPointsInCourse(courseUuid, null, userUuid)).thenReturn(12L);
        when(progressApplicationService.getProgress(userId)).thenReturn(progress);

        UserStatisticsView result = service.getUserStatistics(courseId, groupId, userId);

        assertThat(result.groupId()).isNull();
        assertThat(result.rankInGroup()).isNull();
        assertThat(result.rankInCourse()).isEqualTo(12L);
    }

    @Test
    void getUserStatistics_notEnrolled_throwsException() {
        String userId = "u-not-enrolled";
        String courseId = "PHYS-303";

        User user = User.builder().userId(userId).build();
        Course course = Course.builder().build();

        when(userService.getUserByExternalId(userId)).thenReturn(user);
        when(courseService.findByCourseId(courseId)).thenReturn(course);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(false);

        assertThatThrownBy(() -> service.getUserStatistics(courseId, null, userId))
                .isInstanceOf(UserNotEnrolledInCourseException.class);

        verify(enrollmentService).isUserEnrolledInCourse(user, course);
        verifyNoMoreInteractions(enrollmentService, groupService, progressApplicationService);
    }

    @Test
    void getUserStatistics_userNotFound_throwsFromService() {
        String userId = "missing-user";

        when(userService.getUserByExternalId(userId))
                .thenThrow(new RuntimeException("User not found"));

        assertThatThrownBy(() -> service.getUserStatistics("CS-101", null, userId))
                .isInstanceOf(RuntimeException.class);

        verify(userService).getUserByExternalId(userId);
    }

    @Test
    void getUserStatistics_courseNotFound_throwsFromService() {
        String userId = "u-789";
        String courseId = "UNKNOWN";

        User user = User.builder().userId(userId).build();

        when(userService.getUserByExternalId(userId)).thenReturn(user);
        when(courseService.findByCourseId(courseId))
                .thenThrow(new RuntimeException("Course not found"));

        assertThatThrownBy(() -> service.getUserStatistics(courseId, null, userId))
                .isInstanceOf(RuntimeException.class);

        verify(courseService).findByCourseId(courseId);
    }
}