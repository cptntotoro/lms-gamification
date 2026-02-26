package ru.misis.gamification.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.misis.gamification.model.entity.Course;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.model.entity.UserCourseEnrollment;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserCourseEnrollmentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserCourseEnrollmentRepository enrollmentRepository;

    @Test
    void existsByUserAndCourse_existing_returnsTrue() {
        User user = User.builder()
                .userId("user-123")
                .build();
        em.persistAndFlush(user);

        Course course = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(course);

        UserCourseEnrollment enrollment = UserCourseEnrollment.builder()
                .user(user)
                .course(course)
                .totalPointsInCourse(150)
                .build();
        em.persistAndFlush(enrollment);

        assertThat(enrollmentRepository.existsByUserAndCourse(user, course)).isTrue();
    }

    @Test
    void existsByUserAndCourse_notExisting_returnsFalse() {
        User user = User.builder()
                .userId("user-123")
                .build();
        em.persistAndFlush(user);

        Course course = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(course);

        assertThat(enrollmentRepository.existsByUserAndCourse(user, course)).isFalse();
    }

    @Test
    void findByUserAndCourseCourseId_existing_returnsEnrollment() {
        User user = User.builder()
                .userId("user-123")
                .build();
        em.persistAndFlush(user);

        Course course = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(course);

        UserCourseEnrollment enrollment = UserCourseEnrollment.builder()
                .user(user)
                .course(course)
                .totalPointsInCourse(300)
                .build();
        em.persistAndFlush(enrollment);

        Optional<UserCourseEnrollment> found = enrollmentRepository.findByUserAndCourseCourseId(user, "MATH-101");

        assertThat(found).isPresent();
        assertThat(found.get().getTotalPointsInCourse()).isEqualTo(300);
        assertThat(found.get().getCourse().getCourseId()).isEqualTo("MATH-101");
    }

    @Test
    void findByUserAndCourseCourseId_notExisting_returnsEmpty() {
        User user = User.builder()
                .userId("user-123")
                .build();
        em.persistAndFlush(user);

        Optional<UserCourseEnrollment> found = enrollmentRepository.findByUserAndCourseCourseId(user, "NON-EXIST");

        assertThat(found).isEmpty();
    }
}