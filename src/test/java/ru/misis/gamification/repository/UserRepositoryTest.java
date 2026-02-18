package ru.misis.gamification.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.misis.gamification.model.entity.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUserId_existingUser_shouldReturnUser() {
        String externalUserId = "lms-user-12345";
        User user = User.builder()
                .userId(externalUserId)
                .totalPoints(350)
                .level(4)
                .build();

        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findByUserId(externalUserId);

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(externalUserId);
        assertThat(found.get().getTotalPoints()).isEqualTo(350);
        assertThat(found.get().getLevel()).isEqualTo(4);
    }

    @Test
    void findByUserId_nonExistingUser_shouldReturnEmpty() {
        Optional<User> found = userRepository.findByUserId("non-existing-id");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByUserId_existingUser_shouldReturnTrue() {
        String externalUserId = "lms-user-999";
        User user = User.builder()
                .userId(externalUserId)
                .build();

        entityManager.persist(user);
        entityManager.flush();

        assertThat(userRepository.existsByUserId(externalUserId)).isTrue();
    }

    @Test
    void existsByUserId_nonExistingUser_shouldReturnFalse() {
        assertThat(userRepository.existsByUserId("unknown-user")).isFalse();
    }

    @Test
    void saveAndFindById_shouldWorkCorrectly() {
        User newUser = User.builder()
                .userId("new-lms-user")
                .totalPoints(0)
                .level(1)
                .build();

        User saved = userRepository.save(newUser);

        assertThat(saved.getUuid()).isNotNull();

        Optional<User> foundById = userRepository.findById(saved.getUuid());
        assertThat(foundById).isPresent();
        assertThat(foundById.get().getUserId()).isEqualTo("new-lms-user");
    }
}