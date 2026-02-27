package ru.misis.gamification.service.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import ru.misis.gamification.repository.CourseRepository;
import ru.misis.gamification.repository.GroupRepository;
import ru.misis.gamification.repository.UserCourseEnrollmentRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserCourseEnrollmentRepository enrollmentRepository;

    @InjectMocks
    private UserCourseServiceImpl userCourseService;

    private User user;
    private Course course;
    private Group group;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId("user-123")
                .uuid(java.util.UUID.randomUUID())
                .build();

        course = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .uuid(java.util.UUID.randomUUID())
                .build();

        group = Group.builder()
                .groupId("1-A")
                .displayName("Группа 1-А")
                .course(course)
                .uuid(java.util.UUID.randomUUID())
                .build();

        ReflectionTestUtils.setField(userCourseService, "coursesEnabled", true);
    }

    @Test
    void enrollIfNeeded_coursesEnabled_courseExists_enrollsUser() {
        when(courseRepository.findByCourseId("MATH-101")).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserAndCourse(user, course)).thenReturn(false);

        userCourseService.enrollIfNeeded(user, "MATH-101", null);

        verify(enrollmentRepository).save(argThat(enrollment ->
                enrollment.getUser().equals(user) &&
                        enrollment.getCourse().equals(course) &&
                        enrollment.getGroup() == null &&
                        enrollment.getTotalPointsInCourse() == 0
        ));
    }

    @Test
    void enrollIfNeeded_withGroup_enrollsWithGroup() {
        when(courseRepository.findByCourseId("MATH-101")).thenReturn(Optional.of(course));
        when(groupRepository.findByGroupIdAndCourse("1-A", course)).thenReturn(Optional.of(group));
        when(enrollmentRepository.existsByUserAndCourse(user, course)).thenReturn(false);

        userCourseService.enrollIfNeeded(user, "MATH-101", "1-A");

        verify(enrollmentRepository).save(argThat(enrollment ->
                enrollment.getGroup().equals(group)
        ));
    }

    @Test
    void enrollIfNeeded_coursesDisabled_doesNothing() {
        ReflectionTestUtils.setField(userCourseService, "coursesEnabled", false);

        userCourseService.enrollIfNeeded(user, "MATH-101", null);

        verifyNoInteractions(courseRepository, enrollmentRepository);
    }

    @Test
    void enrollIfNeeded_alreadyEnrolled_doesNothing() {
        when(courseRepository.findByCourseId("MATH-101")).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserAndCourse(user, course)).thenReturn(true);

        userCourseService.enrollIfNeeded(user, "MATH-101", null);

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollIfNeeded_courseNotFound_throwsCourseNotFoundException() {
        when(courseRepository.findByCourseId("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userCourseService.enrollIfNeeded(user, "UNKNOWN", null))
                .isInstanceOf(CourseNotFoundException.class);
    }

    @Test
    void enrollIfNeeded_groupNotFound_throwsGroupNotFoundException() {
        when(courseRepository.findByCourseId("MATH-101")).thenReturn(Optional.of(course));
        when(groupRepository.findByGroupIdAndCourse("UNKNOWN", course)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userCourseService.enrollIfNeeded(user, "MATH-101", "UNKNOWN"))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void addPointsToCourse_validEnrollment_addsPoints() {
        UserCourseEnrollment enrollment = UserCourseEnrollment.builder()
                .user(user)
                .course(course)
                .totalPointsInCourse(200)
                .build();

        when(enrollmentRepository.findByUserAndCourseCourseId(user, "MATH-101"))
                .thenReturn(Optional.of(enrollment));

        userCourseService.addPointsToCourse(user, "MATH-101", 150);

        assertThat(enrollment.getTotalPointsInCourse()).isEqualTo(350);
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void addPointsToCourse_noEnrollment_throwsUserNotEnrolledInCourseException() {
        when(enrollmentRepository.findByUserAndCourseCourseId(user, "MATH-101"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userCourseService.addPointsToCourse(user, "MATH-101", 100))
                .isInstanceOf(UserNotEnrolledInCourseException.class);
    }

    @Test
    void addPointsToCourse_coursesDisabled_doesNothing() {
        ReflectionTestUtils.setField(userCourseService, "coursesEnabled", false);

        userCourseService.addPointsToCourse(user, "MATH-101", 100);

        verifyNoInteractions(enrollmentRepository);
    }

    @Test
    void addPointsToCourse_nonPositivePoints_doesNothing() {
        userCourseService.addPointsToCourse(user, "MATH-101", 0);

        verifyNoInteractions(enrollmentRepository);
    }
}