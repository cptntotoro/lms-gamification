package ru.misis.gamification.service.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.Group;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.CourseNotFoundException;
import ru.misis.gamification.exception.GroupNotFoundException;
import ru.misis.gamification.exception.UserNotEnrolledInCourseException;
import ru.misis.gamification.service.simple.course.CourseService;
import ru.misis.gamification.service.simple.group.GroupService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCourseServiceUnitTest {

    @Mock
    private CourseService courseService;

    @Mock
    private GroupService groupService;

    @Mock
    private UserCourseEnrollmentService enrollmentService;

    @InjectMocks
    private UserCourseServiceImpl userCourseService;

    private User user;
    private Course course;
    private Group group;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .uuid(UUID.randomUUID())
                .userId("user-123")
                .build();

        course = Course.builder()
                .uuid(UUID.randomUUID())
                .courseId("MATH-101")
                .displayName("Математика")
                .build();

        group = Group.builder()
                .uuid(UUID.randomUUID())
                .groupId("PM-21-1")
                .displayName("Поток ПМ-21-1")
                .course(course)
                .build();

        ReflectionTestUtils.setField(userCourseService, "coursesEnabled", true);
    }

    @Test
    void enrollIfNeeded_coursesEnabled_noCourseId_doesNothing() {
        userCourseService.enrollIfNeeded(user, null, null);

        verifyNoInteractions(courseService, groupService, enrollmentService);
    }

    @Test
    void enrollIfNeeded_coursesDisabled_doesNothing() {
        ReflectionTestUtils.setField(userCourseService, "coursesEnabled", false);

        userCourseService.enrollIfNeeded(user, "MATH-101", null);

        verifyNoInteractions(courseService, groupService, enrollmentService);
    }

    @Test
    void enrollIfNeeded_alreadyEnrolled_doesNothing() {
        when(courseService.findByCourseId("MATH-101")).thenReturn(course);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(true);

        userCourseService.enrollIfNeeded(user, "MATH-101", null);

        verify(enrollmentService, never()).save(any());
    }

    @Test
    void enrollIfNeeded_courseNotFound_throwsCourseNotFoundException() {
        when(courseService.findByCourseId("UNKNOWN")).thenThrow(new CourseNotFoundException("UNKNOWN"));

        assertThatThrownBy(() -> userCourseService.enrollIfNeeded(user, "UNKNOWN", null))
                .isInstanceOf(CourseNotFoundException.class);

        verify(courseService).findByCourseId("UNKNOWN");
        verifyNoInteractions(enrollmentService);
    }

    @Test
    void enrollIfNeeded_groupNotFound_throwsGroupNotFoundException() {
        when(courseService.findByCourseId("MATH-101")).thenReturn(course);
        when(groupService.getGroupUuidByExternalIdAndCourseId("UNKNOWN", "MATH-101"))
                .thenThrow(new GroupNotFoundException("UNKNOWN", "MATH-101"));

        assertThatThrownBy(() -> userCourseService.enrollIfNeeded(user, "MATH-101", "UNKNOWN"))
                .isInstanceOf(GroupNotFoundException.class);

        verify(groupService).getGroupUuidByExternalIdAndCourseId("UNKNOWN", "MATH-101");
        verifyNoInteractions(enrollmentService);
    }

    @Test
    void enrollIfNeeded_validNoGroup_createsEnrollment() {
        when(courseService.findByCourseId("MATH-101")).thenReturn(course);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(false);

        userCourseService.enrollIfNeeded(user, "MATH-101", null);

        ArgumentCaptor<UserCourseEnrollment> captor = ArgumentCaptor.forClass(UserCourseEnrollment.class);
        verify(enrollmentService).save(captor.capture());

        UserCourseEnrollment saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getCourse()).isEqualTo(course);
        assertThat(saved.getGroup()).isNull();
        assertThat(saved.getTotalPointsInCourse()).isZero();
    }

    @Test
    void enrollIfNeeded_validWithGroup_createsEnrollmentWithGroup() {
        when(courseService.findByCourseId("MATH-101")).thenReturn(course);
        when(groupService.getGroupUuidByExternalIdAndCourseId("PM-21-1", "MATH-101")).thenReturn(group.getUuid());
        when(groupService.findById(group.getUuid())).thenReturn(group);
        when(enrollmentService.isUserEnrolledInCourse(user, course)).thenReturn(false);

        userCourseService.enrollIfNeeded(user, "MATH-101", "PM-21-1");

        ArgumentCaptor<UserCourseEnrollment> captor = ArgumentCaptor.forClass(UserCourseEnrollment.class);
        verify(enrollmentService).save(captor.capture());

        UserCourseEnrollment saved = captor.getValue();
        assertThat(saved.getGroup()).isEqualTo(group);
    }

    @Test
    void addPointsToCourse_validEnrollment_addsPointsAndSaves() {
        UserCourseEnrollment enrollment = UserCourseEnrollment.builder()
                .user(user)
                .course(course)
                .totalPointsInCourse(200)
                .build();

        when(courseService.findById(course.getUuid())).thenReturn(course);
        when(enrollmentService.findByUserAndCourse(user, course)).thenReturn(enrollment);

        userCourseService.addPointsToCourse(user, course.getUuid(), 150);

        assertThat(enrollment.getTotalPointsInCourse()).isEqualTo(350);
        verify(enrollmentService).save(enrollment);
    }

    @Test
    void addPointsToCourse_nonPositivePoints_doesNothing() {
        userCourseService.addPointsToCourse(user, course.getUuid(), 0);

        verifyNoInteractions(courseService, enrollmentService);
    }

    @Test
    void addPointsToCourse_coursesDisabled_doesNothing() {
        ReflectionTestUtils.setField(userCourseService, "coursesEnabled", false);

        userCourseService.addPointsToCourse(user, course.getUuid(), 100);

        verifyNoInteractions(courseService, enrollmentService);
    }

    @Test
    void addPointsToCourse_noEnrollment_throwsUserNotEnrolledInCourseException() {
        when(courseService.findById(course.getUuid())).thenReturn(course);
        when(enrollmentService.findByUserAndCourse(user, course))
                .thenThrow(new UserNotEnrolledInCourseException(user.getUserId(), course.getCourseId()));

        assertThatThrownBy(() -> userCourseService.addPointsToCourse(user, course.getUuid(), 100))
                .isInstanceOf(UserNotEnrolledInCourseException.class);
    }
}