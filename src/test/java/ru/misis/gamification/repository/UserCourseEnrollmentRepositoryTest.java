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
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.Group;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.model.LeaderboardEntryView;

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

        // Зачисления в группу PM-21-1 (4 студента)
        createEnrollment(alice, 850);
        createEnrollment(bob, 620);
        createEnrollment(carol, 300);
        createEnrollment(eve, 150);

        // David зачислен на курс, но без группы
        em.persistAndFlush(UserCourseEnrollment.builder()
                .user(david)
                .course(mathCourse)
                .group(null)
                .totalPointsInCourse(100)
                .build());

        em.flush();
    }

    private void createEnrollment(User user, int pointsInCourse) {
        em.persistAndFlush(UserCourseEnrollment.builder()
                .user(user)
                .course(mathCourse)
                .group(pmGroup)
                .totalPointsInCourse(pointsInCourse)
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
    void existsByUserAndCourse_nullParameters_returnsFalse() {
        assertThat(repository.existsByUserAndCourse(null, mathCourse)).isFalse();
        assertThat(repository.existsByUserAndCourse(alice, null)).isFalse();
    }

    @Test
    void findByUserAndCourse_existing_returnsEnrollmentWithLoadedRelations() {
        var found = repository.findByUserAndCourse(alice, mathCourse);

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getUserId()).isEqualTo("alice");
        assertThat(found.get().getCourse().getCourseId()).isEqualTo("MATH-101");
        assertThat(found.get().getTotalPointsInCourse()).isEqualTo(850);
        assertThat(found.get().getGroup().getGroupId()).isEqualTo("PM-21-1");
    }

    @Test
    void findByUserAndCourse_nonExisting_returnsEmpty() {
        User stranger = User.builder().userId("stranger").totalPoints(0).level(1).build();
        em.persistAndFlush(stranger);

        assertThat(repository.findByUserAndCourse(stranger, mathCourse)).isEmpty();
    }

    @Test
    void findLeaderboardByCourseAndGroup_groupLeaderboard_correctRanking() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("totalPointsInCourse").descending());

        Page<LeaderboardEntryView> page = repository.findLeaderboardByCourseAndGroup(
                mathCourse.getUuid(), pmGroup.getUuid(), pageable);

        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getContent()).hasSize(4);

        LeaderboardEntryView first = page.getContent().getFirst();
        assertThat(first.getUserId()).isEqualTo("alice");
        assertThat(first.getPointsInCourse()).isEqualTo(850);
        assertThat(first.getRank()).isEqualTo(1L);
        assertThat(first.getGlobalLevel()).isEqualTo(5);

        LeaderboardEntryView second = page.getContent().get(1);
        assertThat(second.getUserId()).isEqualTo("bob");
        assertThat(second.getPointsInCourse()).isEqualTo(620);
        assertThat(second.getRank()).isEqualTo(2L);
    }

    @Test
    void findLeaderboardByCourseAndGroup_courseLeaderboard_includesAll() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<LeaderboardEntryView> page = repository.findLeaderboardByCourseAndGroup(
                mathCourse.getUuid(), null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(5); // 4 в группе + David без группы
        assertThat(page.getContent()).anySatisfy(entry ->
                assertThat(entry.getUserId()).isEqualTo("david")
        );
    }

    @Test
    void findLeaderboardByCourseAndGroup_pagination_worksCorrectly() {
        Pageable firstPage = PageRequest.of(0, 2);
        Page<LeaderboardEntryView> page1 = repository.findLeaderboardByCourseAndGroup(
                mathCourse.getUuid(), pmGroup.getUuid(), firstPage);

        assertThat(page1.getTotalElements()).isEqualTo(4);
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getContent().get(0).getUserId()).isEqualTo("alice");
        assertThat(page1.getContent().get(1).getUserId()).isEqualTo("bob");

        Pageable secondPage = PageRequest.of(1, 2);
        Page<LeaderboardEntryView> page2 = repository.findLeaderboardByCourseAndGroup(
                mathCourse.getUuid(), pmGroup.getUuid(), secondPage);

        assertThat(page2.getContent()).hasSize(2);
        assertThat(page2.getContent().get(0).getUserId()).isEqualTo("carol");
        assertThat(page2.getContent().get(1).getUserId()).isEqualTo("eve");
    }

    @Test
    void findLeaderboardByCourseAndGroup_nonExistentGroup_returnsEmptyPage() {
        Page<LeaderboardEntryView> page = repository.findLeaderboardByCourseAndGroup(
                mathCourse.getUuid(), UUID.randomUUID(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findLeaderboardByCourseAndGroup_nonExistentCourse_returnsEmptyPage() {
        Page<LeaderboardEntryView> page = repository.findLeaderboardByCourseAndGroup(
                UUID.randomUUID(), pmGroup.getUuid(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findLeaderboardByCourseAndGroup_nullCourseUuid_returnsEmptyPage() {
        Page<LeaderboardEntryView> page = repository.findLeaderboardByCourseAndGroup(
                null, pmGroup.getUuid(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findLeaderboardByCourseAndGroup_noEnrollments_returnsEmptyPage() {
        // Удаляем все зачисления
        em.getEntityManager().createQuery("DELETE FROM UserCourseEnrollment").executeUpdate();

        Page<LeaderboardEntryView> page = repository.findLeaderboardByCourseAndGroup(
                mathCourse.getUuid(), pmGroup.getUuid(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }
}