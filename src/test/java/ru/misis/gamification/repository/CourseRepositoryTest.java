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
    void findByExternalCourseId_existing_returnsCourse() {
        String externalId = "MATH-101";
        Course course = Course.builder()
                .courseId(externalId)
                .displayName("Математический анализ")
                .active(true)
                .build();
        em.persistAndFlush(course);

        Optional<Course> found = courseRepository.findByCourseId(externalId);

        assertThat(found).isPresent();
        assertThat(found.get().getCourseId()).isEqualTo(externalId);
        assertThat(found.get().getDisplayName()).isEqualTo("Математический анализ");
    }

    @Test
    void findByExternalCourseId_notExisting_returnsEmpty() {
        Optional<Course> found = courseRepository.findByCourseId("NON-EXISTENT");
        assertThat(found).isEmpty();
    }

    @Test
    void findByExternalCourseId_caseSensitive() {
        Course course = Course.builder()
                .courseId("Math-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(course);

        assertThat(courseRepository.findByCourseId("math-101")).isEmpty();
        assertThat(courseRepository.findByCourseId("Math-101")).isPresent();
    }
}