package ru.misis.gamification.repository;

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

import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUserId_existingUser_returnsUser() {
        String externalId = "lms-user-12345";
        User user = User.builder()
                .userId(externalId)
                .totalPoints(350)
                .level(4)
                .build();

        em.persistAndFlush(user);

        Optional<User> found = userRepository.findByUserId(externalId);

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(externalId);
        assertThat(found.get().getTotalPoints()).isEqualTo(350);
        assertThat(found.get().getLevel()).isEqualTo(4);
        assertThat(found.get().getUuid()).isNotNull();
    }

    @Test
    void findByUserId_nonExisting_returnsEmpty() {
        Optional<User> found = userRepository.findByUserId("non-existing-id");

        assertThat(found).isEmpty();
    }

    @Test
    void findByUserId_null_returnsEmpty() {
        Optional<User> found = userRepository.findByUserId(null);

        assertThat(found).isEmpty();
    }

    @Test
    void findByUserId_emptyString_returnsEmpty() {
        Optional<User> found = userRepository.findByUserId("");

        assertThat(found).isEmpty();
    }

    @Test
    void findByUserIdWithLock_existingUser_returnsUserWithLock() {
        String externalId = "lms-user-locked";
        User user = User.builder()
                .userId(externalId)
                .totalPoints(1000)
                .level(10)
                .build();

        em.persistAndFlush(user);

        Optional<User> found = userRepository.findByUserIdWithLock(externalId);

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(externalId);
        assertThat(found.get().getTotalPoints()).isEqualTo(1000);
        assertThat(found.get().getLevel()).isEqualTo(10);
    }

    @Test
    void findByUserIdWithLock_nonExisting_returnsEmpty() {
        Optional<User> found = userRepository.findByUserIdWithLock("unknown-locked");

        assertThat(found).isEmpty();
    }

    @Test
    void findByUserIdWithLock_null_returnsEmpty() {
        Optional<User> found = userRepository.findByUserIdWithLock(null);

        assertThat(found).isEmpty();
    }

    @Test
    void existsByUserId_existing_returnsTrue() {
        String externalId = "lms-user-exists";
        User user = User.builder().userId(externalId).build();

        em.persistAndFlush(user);

        assertThat(userRepository.existsByUserId(externalId)).isTrue();
    }

    @Test
    void existsByUserId_nonExisting_returnsFalse() {
        assertThat(userRepository.existsByUserId("unknown")).isFalse();
    }

    @Test
    void existsByUserId_null_returnsFalse() {
        assertThat(userRepository.existsByUserId(null)).isFalse();
    }

    @Test
    void existsByUserId_empty_returnsFalse() {
        assertThat(userRepository.existsByUserId("")).isFalse();
    }

    @Test
    void findUuidByUserId_existing_returnsUuid() {
        String externalId = "lms-user-uuid";
        User user = User.builder().userId(externalId).build();

        em.persistAndFlush(user);

        Optional<UUID> uuid = userRepository.findUuidByUserId(externalId);

        assertThat(uuid).isPresent();
        assertThat(uuid.get()).isEqualTo(user.getUuid());
    }

    @Test
    void findUuidByUserId_nonExisting_returnsEmpty() {
        Optional<UUID> uuid = userRepository.findUuidByUserId("missing-uuid");

        assertThat(uuid).isEmpty();
    }

    @Test
    void findUuidByUserId_null_returnsEmpty() {
        Optional<UUID> uuid = userRepository.findUuidByUserId(null);

        assertThat(uuid).isEmpty();
    }

    @Test
    void findUuidByUserId_empty_returnsEmpty() {
        Optional<UUID> uuid = userRepository.findUuidByUserId("");

        assertThat(uuid).isEmpty();
    }

    @Test
    void save_newUser_generatesUuidAndTimestamps() {
        User user = User.builder()
                .userId("new-user")
                .totalPoints(0)
                .level(1)
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        assertThat(saved.getCreatedAt())
                .isCloseTo(saved.getUpdatedAt(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    void findAll_noFilters_returnsAllUsers() {
        persistUser("user1", 100, 2);
        persistUser("user2", 500, 5);
        persistUser("user3", 50, 1);

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> page = userRepository.findAll(null, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent())
                .extracting(User::getUserId)
                .containsExactlyInAnyOrder("user1", "user2", "user3");
    }

    @Test
    void findAll_byCourseId_only_returnsUsersFromCourse() {
        Course course = persistCourse("course-math");
        Group groupA = persistGroup(course, "group-A");
        Group groupB = persistGroup(course, "group-B");

        User user1 = persistUser("user1", 200, 3);
        User user2 = persistUser("user2", 300, 4);

        persistEnrollment(user1, course, groupA);
        persistEnrollment(user2, course, groupB);

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> page = userRepository.findAll("course-math", null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(User::getUserId)
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void findAll_byCourseAndGroup_returnsUsersFromSpecificGroup() {
        // given
        Course course = persistCourse("course-physics");
        Group groupX = persistGroup(course, "group-X");
        Group groupY = persistGroup(course, "group-Y");

        User userA = persistUser("userA", 400, 5);
        User userB = persistUser("userB", 250, 3);
        User userC = persistUser("userC", 100, 2);

        persistEnrollment(userA, course, groupX);
        persistEnrollment(userB, course, groupY);
        persistEnrollment(userC, course, groupX);

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> page = userRepository.findAll("course-physics", "group-X", pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(User::getUserId)
                .containsExactlyInAnyOrder("userA", "userC");
    }

    @Test
    void findAll_byCourseAndGroup_noUsers_returnsEmptyPage() {
        Course course = persistCourse("course-empty");
        persistGroup(course, "group-empty");

        persistUser("lonely-user", 10, 1); // без enrollments

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> page = userRepository.findAll("course-empty", "group-empty", pageable);

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findAll_withPaginationAndSorting_returnsCorrectPageAndOrder() {
        persistUser("user-low", 100, 2);
        persistUser("user-mid", 500, 5);
        persistUser("user-high", 900, 8);
        persistUser("user-extra", 300, 4);

        Pageable pageable = PageRequest.of(0, 2, Sort.by("totalPoints").descending());

        Page<User> page = userRepository.findAll(null, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(User::getUserId)
                .containsExactly("user-high", "user-mid"); // первые 2 по убыванию баллов
    }

    private User persistUser(String userId, int points, int level) {
        User user = User.builder()
                .userId(userId)
                .totalPoints(points)
                .level(level)
                .build();
        return em.persistAndFlush(user);
    }

    private Course persistCourse(String courseId) {
        Course course = Course.builder()
                .courseId(courseId)
                .displayName("Test Course " + courseId)
                .build();
        return em.persistAndFlush(course);
    }

    private Group persistGroup(Course course, String groupId) {
        Group group = Group.builder()
                .groupId(groupId)
                .displayName("Group " + groupId)
                .course(course)
                .build();
        return em.persistAndFlush(group);
    }

    private UserCourseEnrollment persistEnrollment(User user, Course course, Group group) {
        UserCourseEnrollment enrollment = UserCourseEnrollment.builder()
                .user(user)
                .course(course)
                .group(group)
                .totalPointsInCourse(user.getTotalPoints())
                .build();
        return em.persistAndFlush(enrollment);
    }
}