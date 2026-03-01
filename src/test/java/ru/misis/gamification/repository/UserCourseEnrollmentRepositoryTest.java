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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserCourseEnrollmentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserCourseEnrollmentRepository repository;

    private Course mathCourse;
    private Group pmGroup;
    private User alice, bob, carol, david, eve;

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
        eve = User.builder().userId("eve").totalPoints(200).level(2).build();

        em.persist(alice);
        em.persist(bob);
        em.persist(carol);
        em.persist(david);
        em.persist(eve);
        em.flush();

        // Зачисления в группу PM-21-1
        createEnrollment(alice, 850);
        createEnrollment(bob, 620);
        createEnrollment(carol, 300);
        createEnrollment(eve, 150);

        // David зачислен на курс, но без группы → не попадёт в лидерборд по группе
        em.persistAndFlush(UserCourseEnrollment.builder()
                .user(david)
                .course(mathCourse)
                .group(null)
                .totalPointsInCourse(100)
                .build());

        em.flush();
    }

    private void createEnrollment(User user, int points) {
        em.persistAndFlush(UserCourseEnrollment.builder()
                .user(user)
                .course(mathCourse)
                .group(pmGroup)
                .totalPointsInCourse(points)
                .build());
    }

    @Test
    void existsByUserAndCourse_existing_returnsTrue() {
        assertThat(repository.existsByUserAndCourse(alice, mathCourse)).isTrue();
    }

    @Test
    void existsByUserAndCourse_nonExistingUser_returnsFalse() {
        User stranger = User.builder().userId("stranger").totalPoints(0).level(1).build();
        em.persistAndFlush(stranger);

        assertThat(repository.existsByUserAndCourse(stranger, mathCourse)).isFalse();
    }

    @Test
    void existsByUserAndCourse_wrongCourse_returnsFalse() {
        Course physics = Course.builder().courseId("PHYS-202").displayName("Физика").build();
        em.persistAndFlush(physics);

        assertThat(repository.existsByUserAndCourse(alice, physics)).isFalse();
    }

    @Test
    void existsByUserAndCourse_nullUserOrCourse_returnsFalse() {
        assertThat(repository.existsByUserAndCourse(null, mathCourse)).isFalse();
        assertThat(repository.existsByUserAndCourse(alice, null)).isFalse();
    }

    @Test
    void findByUserAndCourse_existing_returnsEnrollment() {
        Optional<UserCourseEnrollment> found = repository.findByUserAndCourse(alice, mathCourse);

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getUuid()).isEqualTo(alice.getUuid());
        assertThat(found.get().getCourse().getUuid()).isEqualTo(mathCourse.getUuid());
        assertThat(found.get().getTotalPointsInCourse()).isEqualTo(850);
    }

    @Test
    void findByUserAndCourse_nonExisting_returnsEmpty() {
        User stranger = User.builder().userId("stranger").totalPoints(0).level(1).build();
        em.persistAndFlush(stranger);

        assertThat(repository.findByUserAndCourse(stranger, mathCourse)).isEmpty();
    }

    @Test
    void findByUserAndCourse_wrongCourse_returnsEmpty() {
        Course physics = Course.builder().courseId("PHYS-202").displayName("Физика").build();
        em.persistAndFlush(physics);

        assertThat(repository.findByUserAndCourse(alice, physics)).isEmpty();
    }

    @Test
    void findByUserAndCourse_nullParameters_returnsEmpty() {
        assertThat(repository.findByUserAndCourse(null, mathCourse)).isEmpty();
        assertThat(repository.findByUserAndCourse(alice, null)).isEmpty();
    }

    @Test
    void findLeaderboardByCourseAndGroup_correctRankingAndPage() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<LeaderboardEntryDto> page = repository.findLeaderboardByCourseAndGroup(
                mathCourse.getUuid(), pmGroup.getUuid(), pageable);

        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getContent()).hasSize(4);

        LeaderboardEntryDto first = page.getContent().getFirst();
        assertThat(first.getUserId()).isEqualTo("alice");
        assertThat(first.getPointsInCourse()).isEqualTo(850);
        assertThat(first.getRank()).isEqualTo(1L);

        LeaderboardEntryDto second = page.getContent().get(1);
        assertThat(second.getUserId()).isEqualTo("bob");
        assertThat(second.getPointsInCourse()).isEqualTo(620);
        assertThat(second.getRank()).isEqualTo(2L);

        assertThat(page.getContent().get(2).getUserId()).isEqualTo("carol");
        assertThat(page.getContent().get(3).getUserId()).isEqualTo("eve");
    }

    @Test
    void findLeaderboardByCourseAndGroup_pagination_correct() {
        Pageable firstPage = PageRequest.of(0, 2);
        Page<LeaderboardEntryDto> page1 = repository.findLeaderboardByCourseAndGroup(
                mathCourse.getUuid(), pmGroup.getUuid(), firstPage);

        assertThat(page1.getTotalElements()).isEqualTo(4);
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getContent().get(0).getUserId()).isEqualTo("alice");
        assertThat(page1.getContent().get(0).getRank()).isEqualTo(1L);
        assertThat(page1.getContent().get(1).getUserId()).isEqualTo("bob");
        assertThat(page1.getContent().get(1).getRank()).isEqualTo(2L);

        Pageable secondPage = PageRequest.of(1, 2);
        Page<LeaderboardEntryDto> page2 = repository.findLeaderboardByCourseAndGroup(
                mathCourse.getUuid(), pmGroup.getUuid(), secondPage);

        assertThat(page2.getContent()).hasSize(2);
        assertThat(page2.getContent().get(0).getUserId()).isEqualTo("carol");
        assertThat(page2.getContent().get(0).getRank()).isEqualTo(3L);
        assertThat(page2.getContent().get(1).getUserId()).isEqualTo("eve");
        assertThat(page2.getContent().get(1).getRank()).isEqualTo(4L);
    }

    @Test
    void findLeaderboardByCourseAndGroup_nonExistentGroup_returnsEmpty() {
        Page<LeaderboardEntryDto> page = repository.findLeaderboardByCourseAndGroup(
                mathCourse.getUuid(), UUID.randomUUID(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findLeaderboardByCourseAndGroup_nonExistentCourse_returnsEmpty() {
        Page<LeaderboardEntryDto> page = repository.findLeaderboardByCourseAndGroup(
                UUID.randomUUID(), pmGroup.getUuid(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findLeaderboardByCourseAndGroup_nullParameters_returnsEmpty() {
        Page<LeaderboardEntryDto> page1 = repository.findLeaderboardByCourseAndGroup(
                null, pmGroup.getUuid(), PageRequest.of(0, 10));

        Page<LeaderboardEntryDto> page2 = repository.findLeaderboardByCourseAndGroup(
                mathCourse.getUuid(), null, PageRequest.of(0, 10));

        assertThat(page1.getTotalElements()).isZero();
        assertThat(page2.getTotalElements()).isZero();
    }

    @Test
    void findLeaderboardByCourseAndGroup_noEnrollmentsInGroup_returnsEmpty() {
        // Удаляем все зачисления в группе PM-21-1
        em.getEntityManager().createQuery(
                "DELETE FROM UserCourseEnrollment uce WHERE uce.group.uuid = :groupUuid"
        ).setParameter("groupUuid", pmGroup.getUuid()).executeUpdate();

        Page<LeaderboardEntryDto> page = repository.findLeaderboardByCourseAndGroup(
                mathCourse.getUuid(), pmGroup.getUuid(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }
}