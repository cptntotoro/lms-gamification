package ru.misis.gamification.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.Group;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GroupRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private GroupRepository groupRepository;

    @Test
    void findByGroupIdAndCourse_existingGroup_returnsGroup() {
        Course mathCourse = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(mathCourse);

        Group group = Group.builder()
                .groupId("PM-21-1")
                .displayName("Поток ПМ-21-1")
                .course(mathCourse)
                .build();
        em.persistAndFlush(group);

        Optional<Group> found = groupRepository.findByGroupIdAndCourse("PM-21-1", mathCourse);

        assertThat(found).isPresent();
        assertThat(found.get().getGroupId()).isEqualTo("PM-21-1");
        assertThat(found.get().getCourse().getUuid()).isEqualTo(mathCourse.getUuid());
        assertThat(found.get().getDisplayName()).isEqualTo("Поток ПМ-21-1");
    }

    @Test
    void findByGroupIdAndCourse_wrongCourse_returnsEmpty() {
        Course mathCourse = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        Course physicsCourse = Course.builder()
                .courseId("PHYS-202")
                .displayName("Физика")
                .build();
        em.persistAndFlush(mathCourse);
        em.persistAndFlush(physicsCourse);

        Group group = Group.builder()
                .groupId("PM-21-1")
                .displayName("Поток ПМ-21-1")
                .course(mathCourse)
                .build();
        em.persistAndFlush(group);

        Optional<Group> found = groupRepository.findByGroupIdAndCourse("PM-21-1", physicsCourse);

        assertThat(found).isEmpty();
    }

    @Test
    void findByGroupIdAndCourse_nonExistentGroupId_returnsEmpty() {
        Course course = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(course);

        Optional<Group> found = groupRepository.findByGroupIdAndCourse("NON-EXIST", course);

        assertThat(found).isEmpty();
    }

    @Test
    void findByGroupIdAndCourse_nullCourse_returnsEmpty() {
        Course course = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(course);

        Group group = Group.builder()
                .groupId("PM-21-1")
                .displayName("Поток ПМ-21-1")
                .course(course)
                .build();
        em.persistAndFlush(group);

        Optional<Group> found = groupRepository.findByGroupIdAndCourse("PM-21-1", null);

        assertThat(found).isEmpty();
    }

    @Test
    void existsByGroupIdAndCourseCourseId_existing_returnsTrue() {
        Course course = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(course);

        Group group = Group.builder()
                .groupId("PM-21-1")
                .displayName("Поток ПМ-21-1")
                .course(course)
                .build();
        em.persistAndFlush(group);

        boolean exists = groupRepository.existsByGroupIdAndCourseCourseId("PM-21-1", "MATH-101");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByGroupIdAndCourseCourseId_wrongCourseId_returnsFalse() {
        Course course = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(course);

        Group group = Group.builder()
                .groupId("PM-21-1")
                .displayName("Поток ПМ-21-1")
                .course(course)
                .build();
        em.persistAndFlush(group);

        boolean exists = groupRepository.existsByGroupIdAndCourseCourseId("PM-21-1", "PHYS-202");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByGroupIdAndCourseCourseId_nonExistentGroupId_returnsFalse() {
        Course course = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(course);

        boolean exists = groupRepository.existsByGroupIdAndCourseCourseId("NON-EXIST", "MATH-101");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByGroupIdAndCourseCourseId_nullCourseId_returnsFalse() {
        Course course = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(course);

        Group group = Group.builder()
                .groupId("PM-21-1")
                .displayName("Поток ПМ-21-1")
                .course(course)
                .build();
        em.persistAndFlush(group);

        boolean exists = groupRepository.existsByGroupIdAndCourseCourseId("PM-21-1", null);

        assertThat(exists).isFalse();
    }

    @Test
    void existsByGroupIdAndCourseCourseId_nullGroupId_returnsFalse() {
        Course course = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(course);

        boolean exists = groupRepository.existsByGroupIdAndCourseCourseId(null, "MATH-101");

        assertThat(exists).isFalse();
    }
}