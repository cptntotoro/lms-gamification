package ru.misis.gamification.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.misis.gamification.entity.Course;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CourseRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void findByCourseId_existingCourse_returnsCourse() {
        String externalId = "MATH-101";
        Course course = Course.builder()
                .courseId(externalId)
                .displayName("Математический анализ")
                .shortName("Матан")
                .description("Основы высшей математики")
                .active(true)
                .build();
        em.persistAndFlush(course);

        Optional<Course> found = courseRepository.findByCourseId(externalId);

        assertThat(found).isPresent();
        Course result = found.get();
        assertThat(result.getCourseId()).isEqualTo(externalId);
        assertThat(result.getDisplayName()).isEqualTo("Математический анализ");
        assertThat(result.getShortName()).isEqualTo("Матан");
        assertThat(result.getDescription()).isEqualTo("Основы высшей математики");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUuid()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void findByCourseId_nonExistentCourse_returnsEmpty() {
        Optional<Course> found = courseRepository.findByCourseId("NON-EXISTENT");
        assertThat(found).isEmpty();
    }

    @Test
    void findByCourseId_caseSensitive_returnsCorrectResult() {
        Course courseUpper = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика верхний регистр")
                .build();
        em.persistAndFlush(courseUpper);

        Course courseLower = Course.builder()
                .courseId("math-101")
                .displayName("Математика нижний регистр")
                .build();
        em.persistAndFlush(courseLower);

        assertThat(courseRepository.findByCourseId("MATH-101")).isPresent();
        assertThat(courseRepository.findByCourseId("MATH-101").get().getDisplayName())
                .isEqualTo("Математика верхний регистр");

        assertThat(courseRepository.findByCourseId("math-101")).isPresent();
        assertThat(courseRepository.findByCourseId("math-101").get().getDisplayName())
                .isEqualTo("Математика нижний регистр");

        assertThat(courseRepository.findByCourseId("Math-101")).isEmpty();
    }

    @Test
    void findByCourseId_emptyString_returnsEmpty() {
        Optional<Course> found = courseRepository.findByCourseId("");
        assertThat(found).isEmpty();
    }

    @Test
    void findByCourseId_null_returnsEmpty() {
        Optional<Course> found = courseRepository.findByCourseId(null);
        assertThat(found).isEmpty();
    }

    @Test
    void existsByCourseId_existingCourse_returnsTrue() {
        Course course = Course.builder()
                .courseId("PHYS-202")
                .displayName("Физика")
                .active(false)
                .build();
        em.persistAndFlush(course);

        boolean exists = courseRepository.existsByCourseId("PHYS-202");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByCourseId_nonExistentCourse_returnsFalse() {
        boolean exists = courseRepository.existsByCourseId("NON-EXISTENT");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByCourseId_caseSensitive() {
        Course course = Course.builder()
                .courseId("HIST-303")
                .displayName("История")
                .build();
        em.persistAndFlush(course);

        assertThat(courseRepository.existsByCourseId("HIST-303")).isTrue();
        assertThat(courseRepository.existsByCourseId("hist-303")).isFalse();
    }

    @Test
    void existsByCourseId_emptyString_returnsFalse() {
        boolean exists = courseRepository.existsByCourseId("");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByCourseId_null_returnsFalse() {
        boolean exists = courseRepository.existsByCourseId(null);
        assertThat(exists).isFalse();
    }

    @Test
    void existsByCourseId_multipleCoursesWithSameId_shouldBeFalseAfterDeletion() {
        String externalId = "BIO-404";

        Course course1 = Course.builder()
                .courseId(externalId)
                .displayName("Биология 1")
                .build();
        em.persistAndFlush(course1);

        boolean existsBefore = courseRepository.existsByCourseId(externalId);
        assertThat(existsBefore).isTrue();

        em.remove(course1);
        em.flush();

        boolean existsAfter = courseRepository.existsByCourseId(externalId);
        assertThat(existsAfter).isFalse();
    }
}