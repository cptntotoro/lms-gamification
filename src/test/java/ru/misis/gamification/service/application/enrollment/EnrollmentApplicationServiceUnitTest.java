package ru.misis.gamification.service.application.enrollment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.Group;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.UserNotEnrolledInCourseException;
import ru.misis.gamification.model.CourseEnrollmentSummary;
import ru.misis.gamification.service.simple.course.CourseService;
import ru.misis.gamification.service.simple.enrollment.EnrollmentService;
import ru.misis.gamification.service.simple.group.GroupService;
import ru.misis.gamification.service.simple.user.UserService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentApplicationServiceUnitTest {

    @Mock
    private CourseService courseService;

    @Mock
    private GroupService groupService;

    @Mock
    private UserService userService;

    @Mock
    private EnrollmentService enrollmentService;

    @Spy
    @InjectMocks
    private EnrollmentApplicationServiceImpl service;

    @Captor
    private ArgumentCaptor<UserCourseEnrollment> enrollmentCaptor;

    private User user;
    private Course course;
    private Group group;
    private UserCourseEnrollment existingEnrollment;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .uuid(UUID.randomUUID())
                .userId("user-123")
                .build();

        course = Course.builder()
                .uuid(UUID.randomUUID())
                .courseId("CS-101")
                .build();

        group = Group.builder()
                .uuid(UUID.randomUUID())
                .groupId("G-14")
                .course(course)
                .build();

        existingEnrollment = UserCourseEnrollment.builder()
                .user(user)
                .course(course)
                .group(group)
                .totalPointsInCourse(500)
                .build();

        ReflectionTestUtils.setField(service, "coursesEnabled", true);
    }

    @Test
    void enrollIfNeeded_courseIdInvalid_doesNothing() {
        service.enrollIfNeeded("user-123", null, "G-14");
        service.enrollIfNeeded("user-123", "  ", "G-14");

        verifyNoInteractions(userService, courseService, groupService, enrollmentService);
    }

    @Test
    void enrollIfNeeded_alreadyEnrolled_skipsAndLogs() {
        when(userService.getUserByExternalId("user-123")).thenReturn(user);
        when(courseService.findByCourseId("CS-101")).thenReturn(course);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(true);

        service.enrollIfNeeded("user-123", "CS-101", "G-14");

        verify(enrollmentService).isUserEnrolledInCourse(user, course);
        verifyNoMoreInteractions(enrollmentService, groupService);
    }

    @Test
    void enrollIfNeeded_noGroupId_createsEnrollmentWithoutGroup() {
        when(userService.getUserByExternalId("user-123")).thenReturn(user);
        when(courseService.findByCourseId("CS-101")).thenReturn(course);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(false);

        service.enrollIfNeeded("user-123", "CS-101", null);

        verify(enrollmentService).save(enrollmentCaptor.capture());

        UserCourseEnrollment saved = enrollmentCaptor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getCourse()).isEqualTo(course);
        assertThat(saved.getGroup()).isNull();
        assertThat(saved.getTotalPointsInCourse()).isZero();
    }

    @Test
    void enrollIfNeeded_withGroupId_createsEnrollmentWithGroup() {
        when(userService.getUserByExternalId("user-123")).thenReturn(user);
        when(courseService.findByCourseId("CS-101")).thenReturn(course);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(false);
        when(groupService.getGroupUuidByExternalIdAndCourseId("G-14", "CS-101")).thenReturn(group.getUuid());
        when(groupService.findById(group.getUuid())).thenReturn(group);

        service.enrollIfNeeded("user-123", "CS-101", "G-14");

        verify(enrollmentService).save(enrollmentCaptor.capture());

        UserCourseEnrollment saved = enrollmentCaptor.getValue();
        assertThat(saved.getGroup()).isEqualTo(group);
        assertThat(saved.getTotalPointsInCourse()).isZero();
    }

    @Test
    void addPointsToCourse_pointsNonPositive_doesNothing() {
        service.addPointsToCourse("user-123", UUID.randomUUID(), 0);
        service.addPointsToCourse("user-123", UUID.randomUUID(), -10);

        verifyNoInteractions(userService, courseService, enrollmentService);
    }

    @Test
    void addPointsToCourse_valid_increasesPointsAndSaves() {
        UUID courseUuid = UUID.randomUUID();

        when(userService.getUserByExternalId("user-123")).thenReturn(user);
        when(courseService.findById(courseUuid)).thenReturn(course);
        when(enrollmentService.findByUserAndCourse(user, course)).thenReturn(existingEnrollment);

        service.addPointsToCourse("user-123", courseUuid, 150);

        verify(enrollmentService).save(enrollmentCaptor.capture());

        UserCourseEnrollment saved = enrollmentCaptor.getValue();
        assertThat(saved.getTotalPointsInCourse()).isEqualTo(500 + 150);
    }

    @Test
    void getEnrollmentSummary_notEnrolled_throwsException() {
        when(userService.getUserByExternalId("user-123")).thenReturn(user);
        when(courseService.findByCourseId("CS-101")).thenReturn(course);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(false);

        assertThatThrownBy(() -> service.getEnrollmentSummary("user-123", "CS-101"))
                .isInstanceOf(UserNotEnrolledInCourseException.class)
                .hasMessageContainingAll("user-123", "CS-101");
    }

    @Test
    void getEnrollmentSummary_withGroup_returnsFullSummary() {
        when(userService.getUserByExternalId("user-123")).thenReturn(user);
        when(courseService.findByCourseId("CS-101")).thenReturn(course);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(true);
        when(enrollmentService.findByUserAndCourse(user, course)).thenReturn(existingEnrollment);

        CourseEnrollmentSummary summary = service.getEnrollmentSummary("user-123", "CS-101");

        assertThat(summary.userUuid()).isEqualTo(user.getUuid());
        assertThat(summary.userId()).isEqualTo("user-123");
        assertThat(summary.courseUuid()).isEqualTo(course.getUuid());
        assertThat(summary.courseId()).isEqualTo("CS-101");
        assertThat(summary.groupUuid()).isEqualTo(group.getUuid());
        assertThat(summary.groupId()).isEqualTo("G-14");
        assertThat(summary.pointsInCourse()).isEqualTo(500);
    }

    @Test
    void getEnrollmentSummary_withoutGroup_returnsSummaryWithoutGroup() {
        existingEnrollment.setGroup(null);

        when(userService.getUserByExternalId("user-123")).thenReturn(user);
        when(courseService.findByCourseId("CS-101")).thenReturn(course);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(true);
        when(enrollmentService.findByUserAndCourse(user, course)).thenReturn(existingEnrollment);

        CourseEnrollmentSummary summary = service.getEnrollmentSummary("user-123", "CS-101");

        assertThat(summary.groupUuid()).isNull();
        assertThat(summary.groupId()).isNull();
        assertThat(summary.pointsInCourse()).isEqualTo(500);
    }
}