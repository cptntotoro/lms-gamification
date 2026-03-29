package ru.misis.gamification.service.application.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.Group;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.CourseNotFoundException;
import ru.misis.gamification.exception.UserNotEnrolledInCourseException;
import ru.misis.gamification.model.UserCoursesView;
import ru.misis.gamification.model.UserProgressView;
import ru.misis.gamification.model.UserStatisticsView;
import ru.misis.gamification.service.simple.course.CourseService;
import ru.misis.gamification.service.simple.enrollment.EnrollmentService;
import ru.misis.gamification.service.simple.group.GroupService;
import ru.misis.gamification.service.simple.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
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
    void getUserStatistics_withGroup() {
        String userId = "u1";
        String courseId = "c1";
        String groupId = "g1";

        UUID userUuid = UUID.randomUUID();
        UUID courseUuid = UUID.randomUUID();
        UUID groupUuid = UUID.randomUUID();

        User user = User.builder().uuid(userUuid).userId(userId).level(3).totalPoints(100).build();
        Course course = Course.builder().uuid(courseUuid).courseId(courseId).build();
        UserCourseEnrollment enrollment = UserCourseEnrollment.builder().totalPointsInCourse(50).build();
        UserProgressView progress = new UserProgressView(userId, 100, 3, 20L, 40.0);

        when(userService.getUserByExternalId(userId)).thenReturn(user);
        when(courseService.findByCourseId(courseId)).thenReturn(course);
        when(groupService.getGroupUuidByExternalIdAndCourseId(groupId, courseId)).thenReturn(groupUuid);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(true);
        when(enrollmentService.findByUserAndCourse(user, course)).thenReturn(enrollment);
        when(enrollmentService.getRankByPointsInCourse(courseUuid, null, userUuid)).thenReturn(10L);
        when(enrollmentService.getRankByPointsInCourse(courseUuid, groupUuid, userUuid)).thenReturn(2L);
        when(progressApplicationService.getProgress(userId)).thenReturn(progress);

        UserStatisticsView result = service.getUserStatistics(userId, courseId, groupId);

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.rankInCourse()).isEqualTo(10L);
        assertThat(result.rankInGroup()).isEqualTo(2L);
        assertThat(result.pointsInCourse()).isEqualTo(50);
    }

    @Test
    void getUserStatistics_withoutGroup() {
        String userId = "u1";
        String courseId = "c1";

        UUID userUuid = UUID.randomUUID();
        UUID courseUuid = UUID.randomUUID();

        User user = User.builder().uuid(userUuid).userId(userId).level(1).totalPoints(10).build();
        Course course = Course.builder().uuid(courseUuid).courseId(courseId).build();
        UserCourseEnrollment enrollment = UserCourseEnrollment.builder().totalPointsInCourse(5).build();
        UserProgressView progress = new UserProgressView(userId, 10, 1, 5L, 50.0);

        when(userService.getUserByExternalId(userId)).thenReturn(user);
        when(courseService.findByCourseId(courseId)).thenReturn(course);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(true);
        when(enrollmentService.findByUserAndCourse(user, course)).thenReturn(enrollment);
        when(enrollmentService.getRankByPointsInCourse(courseUuid, null, userUuid)).thenReturn(1L);
        when(progressApplicationService.getProgress(userId)).thenReturn(progress);

        UserStatisticsView result = service.getUserStatistics(userId, courseId, null);

        assertThat(result.rankInGroup()).isNull();
    }

    @Test
    void getUserStatistics_courseNotFound() {
        when(courseService.findByCourseId("c")).thenReturn(null);

        when(userService.getUserByExternalId("u"))
                .thenReturn(User.builder().build());

        assertThatThrownBy(() -> service.getUserStatistics("u", "c", null))
                .isInstanceOf(CourseNotFoundException.class);
    }

    @Test
    void getUserStatistics_notEnrolled() {
        User user = User.builder().build();
        Course course = Course.builder().build();

        when(userService.getUserByExternalId("u")).thenReturn(user);
        when(courseService.findByCourseId("c")).thenReturn(course);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(false);

        assertThatThrownBy(() -> service.getUserStatistics("u", "c", null))
                .isInstanceOf(UserNotEnrolledInCourseException.class);
    }

    @Test
    void getUserCourses_withGroupAndSorting() {
        String userId = "u1";
        UUID courseUuid = UUID.randomUUID();
        UUID groupUuid = UUID.randomUUID();

        User user = User.builder().userId(userId).build();

        Course course = Course.builder()
                .uuid(courseUuid)
                .courseId("c1")
                .displayName("Course")
                .build();

        Group group = Group.builder()
                .uuid(groupUuid)
                .groupId("g1")
                .build();

        UserCourseEnrollment e1 = UserCourseEnrollment.builder()
                .course(course)
                .group(group)
                .totalPointsInCourse(10)
                .enrolledAt(LocalDateTime.now().minusSeconds(100))
                .build();

        UserCourseEnrollment e2 = UserCourseEnrollment.builder()
                .course(course)
                .group(null)
                .totalPointsInCourse(20)
                .enrolledAt(LocalDateTime.now())
                .build();

        UserProgressView progress = new UserProgressView(userId, 100, 2, 10L, 60.0);

        when(userService.getUserByExternalId(userId)).thenReturn(user);
        when(progressApplicationService.getProgress(userId)).thenReturn(progress);
        when(enrollmentService.findAllByUser(user)).thenReturn(List.of(e1, e2));

        when(courseService.findById(courseUuid)).thenReturn(course);
        when(groupService.findById(groupUuid)).thenReturn(group);

        UserCoursesView result = service.getUserCourses(userId);

        assertThat(result.getCourses()).hasSize(2);
        assertThat(result.getCourses().get(0).getTotalPointsInCourse()).isEqualTo(20);
        assertThat(result.getCourses().get(1).getGroupId()).isEqualTo("g1");
    }

    @Test
    void getUserCourses_empty() {
        String userId = "u";

        User user = User.builder().userId(userId).build();
        UserProgressView progress = new UserProgressView(userId, 0, 0, 0L, 0.0);

        when(userService.getUserByExternalId(userId)).thenReturn(user);
        when(progressApplicationService.getProgress(userId)).thenReturn(progress);
        when(enrollmentService.findAllByUser(user)).thenReturn(List.of());

        UserCoursesView result = service.getUserCourses(userId);

        assertThat(result.getCourses()).isEmpty();
    }
}