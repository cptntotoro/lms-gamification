package ru.misis.gamification.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .uuid(UUID.randomUUID())
                .userId("user-777")
                .totalPoints(150)
                .level(2)
                .build();
    }

    @Test
    void createIfNotExists_userAlreadyExists_shouldReturnExisting() {
        when(userRepository.findByUserIdWithLock("user-777"))
                .thenReturn(Optional.of(existingUser));

        User result = userService.createIfNotExists("user-777");

        assertThat(result).isSameAs(existingUser);

        verify(userRepository).findByUserIdWithLock("user-777");
        verify(userRepository, never()).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void createIfNotExists_userNotExists_shouldCreateAndSaveNew() {
        String newUserId = "new-user-999";

        when(userRepository.findByUserIdWithLock(newUserId)).thenReturn(Optional.empty());

        User savedUser = User.builder()
                .uuid(UUID.randomUUID())
                .userId(newUserId)
                .totalPoints(0)
                .level(1)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createIfNotExists(newUserId);

        assertThat(result.getUserId()).isEqualTo(newUserId);
        assertThat(result.getTotalPoints()).isZero();
        assertThat(result.getLevel()).isEqualTo(1);

        verify(userRepository).findByUserIdWithLock(newUserId);
        verify(userRepository).save(argThat(user ->
                user.getUserId().equals(newUserId) &&
                        user.getTotalPoints() == 0 &&
                        user.getLevel() == 1
        ));
    }

    @Test
    void get_existingUser_shouldReturnUser() {
        String userId = "user-ok";

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(existingUser));

        User result = userService.get(userId);

        assertThat(result).isSameAs(existingUser);
        verify(userRepository).findByUserId(userId);
    }

    @Test
    void get_nonExistingUser_shouldThrowException() {
        String userId = "unknown-user";

        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.get(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByUserId(userId);
    }

    @Test
    void getOrCreateLocked_userExists_shouldReturnExisting() {
        String userId = "user-777";

        when(userRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(existingUser));

        User result = userService.getOrCreateLocked(userId);

        assertThat(result).isSameAs(existingUser);

        verify(userRepository).findByUserIdWithLock(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getOrCreateLocked_userNotExists_shouldCreateNew() {
        String userId = "new-user-999";

        when(userRepository.findByUserIdWithLock(userId)).thenReturn(Optional.empty());

        User savedUser = User.builder()
                .uuid(UUID.randomUUID())
                .userId(userId)
                .totalPoints(0)
                .level(1)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.getOrCreateLocked(userId);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTotalPoints()).isZero();
        assertThat(result.getLevel()).isEqualTo(1);

        verify(userRepository).findByUserIdWithLock(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_validUser_shouldSaveAndReturn() {
        User userToUpdate = User.builder()
                .uuid(UUID.randomUUID())
                .userId("user-123")
                .totalPoints(500)
                .level(5)
                .build();

        User saved = User.builder()
                .uuid(userToUpdate.getUuid())
                .userId("user-123")
                .totalPoints(550)
                .level(6)
                .build();

        when(userRepository.save(userToUpdate)).thenReturn(saved);

        User result = userService.update(userToUpdate);

        assertThat(result).isSameAs(saved);
        assertThat(result.getTotalPoints()).isEqualTo(550);
        assertThat(result.getLevel()).isEqualTo(6);

        verify(userRepository).save(userToUpdate);
    }

    @Test
    void update_noUuid_shouldThrowIllegalArgumentException() {
        User invalidUser = User.builder()
                .userId("user-no-uuid")
                .totalPoints(300)
                .build();

        assertThatThrownBy(() -> userService.update(invalidUser))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_nullUser_shouldThrowNullPointerException() {
        assertThatThrownBy(() -> userService.update(null))
                .isInstanceOf(NullPointerException.class);
    }
}