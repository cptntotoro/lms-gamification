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
import java.util.UUID;

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

        Optional<Group> found = groupRepository.findByGroupIdAndCourse("PM-21-1", course);

        assertThat(found).isPresent();
        assertThat(found.get().getGroupId()).isEqualTo("PM-21-1");
        assertThat(found.get().getCourse().getUuid()).isEqualTo(course.getUuid());
        assertThat(found.get().getDisplayName()).isEqualTo("Поток ПМ-21-1");
    }

    @Test
    void findByGroupIdAndCourse_wrongCourse_returnsEmpty() {
        Course math = Course.builder().courseId("MATH-101").displayName("Математика").build();
        Course phys = Course.builder().courseId("PHYS-202").displayName("Физика").build();
        em.persistAndFlush(math);
        em.persistAndFlush(phys);

        Group group = Group.builder()
                .groupId("PM-21-1")
                .displayName("Поток ПМ-21-1")
                .course(math)
                .build();
        em.persistAndFlush(group);

        Optional<Group> found = groupRepository.findByGroupIdAndCourse("PM-21-1", phys);

        assertThat(found).isEmpty();
    }

    @Test
    void findByGroupIdAndCourse_nonExistentGroupId_returnsEmpty() {
        Course course = Course.builder().courseId("MATH-101").displayName("Математика").build();
        em.persistAndFlush(course);

        Optional<Group> found = groupRepository.findByGroupIdAndCourse("NON-EXIST", course);

        assertThat(found).isEmpty();
    }

    @Test
    void findByGroupIdAndCourse_nullCourse_returnsEmpty() {
        Course course = Course.builder().courseId("MATH-101").displayName("Математика").build();
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
    void findByGroupIdAndCourse_nullGroupId_returnsEmpty() {
        Course course = Course.builder().courseId("MATH-101").displayName("Математика").build();
        em.persistAndFlush(course);

        Optional<Group> found = groupRepository.findByGroupIdAndCourse(null, course);

        assertThat(found).isEmpty();
    }

    @Test
    void existsByGroupIdAndCourseCourseId_existing_returnsTrue() {
        Course course = Course.builder().courseId("MATH-101").displayName("Математика").build();
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
        Course course = Course.builder().courseId("MATH-101").displayName("Математика").build();
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
        Course course = Course.builder().courseId("MATH-101").displayName("Математика").build();
        em.persistAndFlush(course);

        boolean exists = groupRepository.existsByGroupIdAndCourseCourseId("NON-EXIST", "MATH-101");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByGroupIdAndCourseCourseId_nullCourseId_returnsFalse() {
        Course course = Course.builder().courseId("MATH-101").displayName("Математика").build();
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
        Course course = Course.builder().courseId("MATH-101").displayName("Математика").build();
        em.persistAndFlush(course);

        boolean exists = groupRepository.existsByGroupIdAndCourseCourseId(null, "MATH-101");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByGroupIdAndCourseCourseId_caseSensitive() {
        Course course = Course.builder().courseId("MATH-101").displayName("Математика").build();
        em.persistAndFlush(course);

        Group group = Group.builder()
                .groupId("PM-21-1")
                .displayName("Поток ПМ-21-1")
                .course(course)
                .build();
        em.persistAndFlush(group);

        assertThat(groupRepository.existsByGroupIdAndCourseCourseId("PM-21-1", "MATH-101")).isTrue();
        assertThat(groupRepository.existsByGroupIdAndCourseCourseId("pm-21-1", "MATH-101")).isFalse();
        assertThat(groupRepository.existsByGroupIdAndCourseCourseId("PM-21-1", "math-101")).isFalse();
    }

    @Test
    void findUuidByGroupIdAndCourseCourseId_existing_returnsUuid() {
        Course course = Course.builder().courseId("MATH-101").displayName("Математика").build();
        em.persistAndFlush(course);

        Group group = Group.builder()
                .groupId("PM-21-1")
                .displayName("Поток ПМ-21-1")
                .course(course)
                .build();
        em.persistAndFlush(group);

        Optional<UUID> uuid = groupRepository.findUuidByGroupIdAndCourseCourseId("PM-21-1", "MATH-101");

        assertThat(uuid).isPresent();
        assertThat(uuid.get()).isEqualTo(group.getUuid());
    }

    @Test
    void findUuidByGroupIdAndCourseCourseId_caseSensitive() {
        Course course = Course.builder().courseId("MATH-101").displayName("Математика").build();
        em.persistAndFlush(course);

        Group groupUpper = Group.builder()
                .groupId("PM-21-1")
                .displayName("Верхний")
                .course(course)
                .build();
        em.persistAndFlush(groupUpper);

        Group groupLower = Group.builder()
                .groupId("pm-21-1")
                .displayName("Нижний")
                .course(course)
                .build();
        em.persistAndFlush(groupLower);

        assertThat(groupRepository.findUuidByGroupIdAndCourseCourseId("PM-21-1", "MATH-101"))
                .isPresent()
                .hasValueSatisfying(u -> assertThat(groupRepository.findById(u).get().getDisplayName()).isEqualTo("Верхний"));

        assertThat(groupRepository.findUuidByGroupIdAndCourseCourseId("pm-21-1", "MATH-101"))
                .isPresent()
                .hasValueSatisfying(u -> assertThat(groupRepository.findById(u).get().getDisplayName()).isEqualTo("Нижний"));

        assertThat(groupRepository.findUuidByGroupIdAndCourseCourseId("Pm-21-1", "MATH-101")).isEmpty();
    }

    @Test
    void findUuidByGroupIdAndCourseCourseId_nonExisting_returnsEmpty() {
        assertThat(groupRepository.findUuidByGroupIdAndCourseCourseId("NON-EXIST", "MATH-101")).isEmpty();
    }

    @Test
    void findUuidByGroupIdAndCourseCourseId_nullGroupId_returnsEmpty() {
        assertThat(groupRepository.findUuidByGroupIdAndCourseCourseId(null, "MATH-101")).isEmpty();
    }

    @Test
    void findUuidByGroupIdAndCourseCourseId_nullCourseId_returnsEmpty() {
        assertThat(groupRepository.findUuidByGroupIdAndCourseCourseId("PM-21-1", null)).isEmpty();
    }

    @Test
    void findUuidByGroupIdAndCourseCourseId_emptyStrings_returnsEmpty() {
        assertThat(groupRepository.findUuidByGroupIdAndCourseCourseId("", "MATH-101")).isEmpty();
        assertThat(groupRepository.findUuidByGroupIdAndCourseCourseId("PM-21-1", "")).isEmpty();
    }
}