package ru.misis.gamification.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.misis.gamification.entity.Course;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void findByCourseId_existing_returnsCourse() {
        Course saved = courseRepository.save(Course.builder()
                .courseId("CS-101")
                .displayName("Алгоритмы и структуры данных")
                .shortName("Алгос")
                .description("Классический курс")
                .active(true)
                .build());

        Optional<Course> found = courseRepository.findByCourseId("CS-101");

        assertThat(found).isPresent();
        assertThat(found.get().getUuid()).isEqualTo(saved.getUuid());
        assertThat(found.get().getDisplayName()).isEqualTo("Алгоритмы и структуры данных");
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    void findByCourseId_caseSensitive() {
        courseRepository.save(Course.builder().courseId("MATH-202").displayName("Математика").build());
        courseRepository.save(Course.builder().courseId("math-202").displayName("математика").build());

        assertThat(courseRepository.findByCourseId("MATH-202"))
                .isPresent().hasValueSatisfying(c -> assertThat(c.getDisplayName()).isEqualTo("Математика"));
        assertThat(courseRepository.findByCourseId("math-202"))
                .isPresent().hasValueSatisfying(c -> assertThat(c.getDisplayName()).isEqualTo("математика"));
        assertThat(courseRepository.findByCourseId("Math-202")).isEmpty();
    }

    @Test
    void findByCourseId_notFound_returnsEmpty() {
        assertThat(courseRepository.findByCourseId("NON-EXISTENT")).isEmpty();
    }

    @Test
    void findByCourseId_null_returnsEmpty() {
        assertThat(courseRepository.findByCourseId(null)).isEmpty();
    }

    @Test
    void findByCourseId_emptyString_returnsEmpty() {
        assertThat(courseRepository.findByCourseId("")).isEmpty();
    }

    @Test
    void existsByCourseId_existing_returnsTrue() {
        courseRepository.save(Course.builder().courseId("PHYS-303").build());
        assertThat(courseRepository.existsByCourseId("PHYS-303")).isTrue();
    }

    @Test
    void existsByCourseId_caseSensitive() {
        courseRepository.save(Course.builder().courseId("HIST-404").build());
        assertThat(courseRepository.existsByCourseId("HIST-404")).isTrue();
        assertThat(courseRepository.existsByCourseId("hist-404")).isFalse();
    }

    @Test
    void existsByCourseId_notFound_returnsFalse() {
        assertThat(courseRepository.existsByCourseId("UNKNOWN")).isFalse();
    }

    @Test
    void existsByCourseId_null_returnsFalse() {
        assertThat(courseRepository.existsByCourseId(null)).isFalse();
    }

    @Test
    void existsByCourseId_empty_returnsFalse() {
        assertThat(courseRepository.existsByCourseId("")).isFalse();
    }

    @Test
    void findUuidByCourseId_existing_returnsUuid() {
        Course saved = courseRepository.save(Course.builder()
                .courseId("PROG-505")
                .displayName("Python")
                .build());

        Optional<UUID> uuid = courseRepository.findUuidByCourseId("PROG-505");

        assertThat(uuid).isPresent();
        assertThat(uuid.get()).isEqualTo(saved.getUuid());
    }

    @Test
    void findUuidByCourseId_caseSensitive() {
        courseRepository.save(Course.builder().courseId("JAVA-606").build());
        courseRepository.save(Course.builder().courseId("java-606").build());

        assertThat(courseRepository.findUuidByCourseId("JAVA-606")).isPresent();
        assertThat(courseRepository.findUuidByCourseId("java-606")).isPresent();
        assertThat(courseRepository.findUuidByCourseId("Java-606")).isEmpty();
    }

    @Test
    void findUuidByCourseId_notFound_returnsEmpty() {
        assertThat(courseRepository.findUuidByCourseId("MISSING")).isEmpty();
    }

    @Test
    void findUuidByCourseId_null_returnsEmpty() {
        assertThat(courseRepository.findUuidByCourseId(null)).isEmpty();
    }

    @Test
    void findUuidByCourseId_empty_returnsEmpty() {
        assertThat(courseRepository.findUuidByCourseId("")).isEmpty();
    }

    @Test
    void save_newCourse_generatesUuidAndTimestamps() {
        Course course = Course.builder()
                .courseId("CHEM-707")
                .displayName("Органическая химия")
                .build();

        Course saved = courseRepository.save(course);

        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        assertThat(saved.getCreatedAt())
                .isCloseTo(saved.getUpdatedAt(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    void save_updateCourse_updatesUpdatedAt() {
        Course course = Course.builder()
                .courseId("BIO-808")
                .displayName("Биология")
                .shortName("Биол")
                .build();

        Course saved = courseRepository.saveAndFlush(course);
        LocalDateTime initialUpdatedAt = saved.getUpdatedAt();

        course.setDisplayName("Биология клетки");
        Course updated = courseRepository.saveAndFlush(course);

        assertThat(updated.getUpdatedAt())
                .as("updatedAt должен обновиться после изменения сущности")
                .isAfterOrEqualTo(initialUpdatedAt)
                .isNotEqualTo(initialUpdatedAt)
                .isAfterOrEqualTo(saved.getCreatedAt());
    }

    @Test
    void save_duplicateCourseId_throwsConstraintViolation() {
        courseRepository.save(Course.builder().courseId("DUPL-999").build());

        assertThatThrownBy(() ->
                courseRepository.saveAndFlush(Course.builder().courseId("DUPL-999").build())
        ).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}