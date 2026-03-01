package ru.misis.gamification.service.course;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.UserCourseEnrollmentNotFoundException;
import ru.misis.gamification.repository.UserCourseEnrollmentRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCourseEnrollmentServiceUnitTest {

    @Mock
    private UserCourseEnrollmentRepository repository;

    @InjectMocks
    private UserCourseEnrollmentServiceImpl service;

    private final User user = User.builder().uuid(UUID.randomUUID()).userId("user-123").build();
    private final Course course = Course.builder().uuid(UUID.randomUUID()).courseId("MATH-101").build();

    @Test
    void isUserEnrolledInCourse_existingEnrollment_returnsTrue() {
        when(repository.existsByUserAndCourse(user, course)).thenReturn(true);

        boolean result = service.isUserEnrolledInCourse(user, course);

        assertThat(result).isTrue();
        verify(repository).existsByUserAndCourse(user, course);
    }

    @Test
    void isUserEnrolledInCourse_noEnrollment_returnsFalse() {
        when(repository.existsByUserAndCourse(user, course)).thenReturn(false);

        boolean result = service.isUserEnrolledInCourse(user, course);

        assertThat(result).isFalse();
        verify(repository).existsByUserAndCourse(user, course);
    }

    @Test
    void findByUserAndCourse_existingEnrollment_returnsEnrollment() {
        UserCourseEnrollment enrollment = UserCourseEnrollment.builder()
                .user(user)
                .course(course)
                .totalPointsInCourse(500)
                .build();

        when(repository.findByUserAndCourse(user, course)).thenReturn(Optional.of(enrollment));

        UserCourseEnrollment result = service.findByUserAndCourse(user, course);

        assertThat(result).isNotNull();
        assertThat(result.getTotalPointsInCourse()).isEqualTo(500);
        verify(repository).findByUserAndCourse(user, course);
    }

    @Test
    void findByUserAndCourse_noEnrollment_throwsUserCourseEnrollmentNotFoundException() {
        when(repository.findByUserAndCourse(user, course)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByUserAndCourse(user, course))
                .isInstanceOf(UserCourseEnrollmentNotFoundException.class)
                .hasMessageContaining(user.getUserId())
                .hasMessageContaining(course.getCourseId());

        verify(repository).findByUserAndCourse(user, course);
    }

    @Test
    void save_enrollment_returnsSavedEnrollment() {
        UserCourseEnrollment enrollment = UserCourseEnrollment.builder()
                .user(user)
                .course(course)
                .totalPointsInCourse(0)
                .build();

        UserCourseEnrollment saved = UserCourseEnrollment.builder()
                .uuid(UUID.randomUUID())
                .user(user)
                .course(course)
                .totalPointsInCourse(0)
                .build();

        when(repository.save(enrollment)).thenReturn(saved);

        UserCourseEnrollment result = service.save(enrollment);

        assertThat(result.getUuid()).isNotNull();
        verify(repository).save(enrollment);
    }
}