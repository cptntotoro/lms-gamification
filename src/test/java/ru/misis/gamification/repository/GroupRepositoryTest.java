package ru.misis.gamification.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.misis.gamification.model.entity.Course;
import ru.misis.gamification.model.entity.Group;

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
    void findByGroupIdAndCourse_existing_returnsGroup() {
        Course course = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(course);

        Group group = Group.builder()
                .groupId("1-A")
                .displayName("Группа 1-А")
                .course(course)
                .build();
        em.persistAndFlush(group);

        Optional<Group> found = groupRepository.findByGroupIdAndCourse("1-A", course);

        assertThat(found).isPresent();
        assertThat(found.get().getGroupId()).isEqualTo("1-A");
        assertThat(found.get().getCourse().getCourseId()).isEqualTo("MATH-101");
    }

    @Test
    void findByGroupIdAndCourse_wrongCourse_returnsEmpty() {
        Course courseA = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        Course courseB = Course.builder()
                .courseId("PHYS-202")
                .displayName("Физика")
                .build();
        em.persistAndFlush(courseA);
        em.persistAndFlush(courseB);

        Group group = Group.builder()
                .groupId("1-A")
                .displayName("Группа 1-А")
                .course(courseA)
                .build();
        em.persistAndFlush(group);

        Optional<Group> found = groupRepository.findByGroupIdAndCourse("1-A", courseB);

        assertThat(found).isEmpty();
    }

    @Test
    void findByGroupIdAndCourse_notExisting_returnsEmpty() {
        Course course = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(course);

        Optional<Group> found = groupRepository.findByGroupIdAndCourse("NON-EXIST", course);

        assertThat(found).isEmpty();
    }
}