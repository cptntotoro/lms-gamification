package ru.misis.gamification.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import ru.misis.gamification.dto.analytics.LeaderboardEntryDto;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.Group;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserCourseEnrollmentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserCourseEnrollmentRepository enrollmentRepository;

    private Course mathCourse;
    private Group pmGroup;
    private User alice;
    private User bob;
    private User carol;
    private User david;

    @BeforeEach
    void setUp() {
        mathCourse = Course.builder()
                .courseId("MATH-101")
                .displayName("Математика")
                .build();
        em.persistAndFlush(mathCourse);

        pmGroup = Group.builder()
                .groupId("PM-21-1")
                .displayName("Поток ПМ-21-1")
                .course(mathCourse)
                .build();
        em.persistAndFlush(pmGroup);

        alice = User.builder().userId("alice").totalPoints(1200).level(5).build();
        bob = User.builder().userId("bob").totalPoints(800).level(4).build();
        carol = User.builder().userId("carol").totalPoints(450).level(3).build();
        david = User.builder().userId("david").totalPoints(0).level(1).build();

        em.persistAndFlush(alice);
        em.persistAndFlush(bob);
        em.persistAndFlush(carol);
        em.persistAndFlush(david);

        // Зачисления и очки по курсу
        em.persistAndFlush(UserCourseEnrollment.builder()
                .user(alice)
                .course(mathCourse)
                .group(pmGroup)
                .totalPointsInCourse(850)
                .build());

        em.persistAndFlush(UserCourseEnrollment.builder()
                .user(bob)
                .course(mathCourse)
                .group(pmGroup)
                .totalPointsInCourse(620)
                .build());

        em.persistAndFlush(UserCourseEnrollment.builder()
                .user(carol)
                .course(mathCourse)
                .group(pmGroup)
                .totalPointsInCourse(300)
                .build());

        // David не зачислен в группу → не должен попасть в лидерборд
        em.persistAndFlush(UserCourseEnrollment.builder()
                .user(david)
                .course(mathCourse)
                .group(null) // другая группа или без группы
                .totalPointsInCourse(100)
                .build());

        em.flush();
    }

    @Test
    void findLeaderboardByCourseAndGroup_shouldReturnCorrectlyRankedPage() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<LeaderboardEntryDto> page = enrollmentRepository.findLeaderboardByCourseAndGroup(
                "MATH-101", "PM-21-1", pageable);

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(3); // только Alice, Bob, Carol
        assertThat(page.getContent()).hasSize(3);

        // Проверяем ранжирование (глобальный rank)
        LeaderboardEntryDto first = page.getContent().getFirst();
        assertThat(first.getUserId()).isEqualTo("alice");
        assertThat(first.getPointsInCourse()).isEqualTo(850);
        assertThat(first.getRank()).isEqualTo(1L);

        LeaderboardEntryDto second = page.getContent().get(1);
        assertThat(second.getUserId()).isEqualTo("bob");
        assertThat(second.getPointsInCourse()).isEqualTo(620);
        assertThat(second.getRank()).isEqualTo(2L);

        LeaderboardEntryDto third = page.getContent().get(2);
        assertThat(third.getUserId()).isEqualTo("carol");
        assertThat(third.getPointsInCourse()).isEqualTo(300);
        assertThat(third.getRank()).isEqualTo(3L);
    }

    @Test
    void findLeaderboardByCourseAndGroup_emptyGroup_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<LeaderboardEntryDto> page = enrollmentRepository.findLeaderboardByCourseAndGroup(
                "MATH-101", "NON-EXISTENT-GROUP", pageable);

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findLeaderboardByCourseAndGroup_nonExistentCourse_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<LeaderboardEntryDto> page = enrollmentRepository.findLeaderboardByCourseAndGroup(
                "PHYS-999", "PM-21-1", pageable);

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findLeaderboardByCourseAndGroup_pagination_worksCorrectly() {
        // Добавим ещё одного студента для проверки пагинации
        User eve = User.builder().userId("eve").totalPoints(200).level(2).build();
        em.persistAndFlush(eve);

        em.persistAndFlush(UserCourseEnrollment.builder()
                .user(eve)
                .course(mathCourse)
                .group(pmGroup)
                .totalPointsInCourse(150)
                .build());

        em.flush();

        // Первая страница (size=2)
        Page<LeaderboardEntryDto> page1 = enrollmentRepository.findLeaderboardByCourseAndGroup(
                "MATH-101", "PM-21-1", PageRequest.of(0, 2));

        assertThat(page1.getTotalElements()).isEqualTo(4);
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getContent().get(0).getUserId()).isEqualTo("alice");
        assertThat(page1.getContent().get(0).getRank()).isEqualTo(1L);
        assertThat(page1.getContent().get(1).getUserId()).isEqualTo("bob");
        assertThat(page1.getContent().get(1).getRank()).isEqualTo(2L);

        // Вторая страница
        Page<LeaderboardEntryDto> page2 = enrollmentRepository.findLeaderboardByCourseAndGroup(
                "MATH-101", "PM-21-1", PageRequest.of(1, 2));

        assertThat(page2.getContent()).hasSize(2);
        assertThat(page2.getContent().get(0).getUserId()).isEqualTo("carol");
        assertThat(page2.getContent().get(0).getRank()).isEqualTo(3L);
        assertThat(page2.getContent().get(1).getUserId()).isEqualTo("eve");
        assertThat(page2.getContent().get(1).getRank()).isEqualTo(4L);
    }

    @Test
    void findLeaderboardByCourseAndGroup_noEnrollments_returnsEmpty() {
        em.getEntityManager().createQuery("DELETE FROM UserCourseEnrollment").executeUpdate();

        Page<LeaderboardEntryDto> page = enrollmentRepository.findLeaderboardByCourseAndGroup(
                "MATH-101", "PM-21-1", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }
}