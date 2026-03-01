package ru.misis.gamification.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.misis.gamification.entity.User;

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
}