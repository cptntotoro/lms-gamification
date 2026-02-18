package ru.misis.gamification.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.repository.UserRepository;
import ru.misis.gamification.service.user.UserServiceImpl;

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

    @Test
    void createIfNotExists_userAlreadyExists_shouldReturnExisting() {
        String externalId = "user-777";
        User existing = User.builder()
                .uuid(UUID.randomUUID())
                .userId(externalId)
                .totalPoints(150)
                .level(2)
                .build();

        when(userRepository.findByUserId(externalId)).thenReturn(Optional.of(existing));

        User result = userService.createIfNotExists(externalId);

        assertThat(result).isSameAs(existing);
        verify(userRepository).findByUserId(externalId);
        verifyNoMoreInteractions(userRepository);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createIfNotExists_userNotExists_shouldCreateAndSaveNew() {
        String externalId = "new-user-999";

        when(userRepository.findByUserId(externalId)).thenReturn(Optional.empty());

        User savedUser = User.builder()
                .uuid(UUID.randomUUID())
                .userId(externalId)
                .totalPoints(0)
                .level(1)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createIfNotExists(externalId);

        assertThat(result.getUserId()).isEqualTo(externalId);
        assertThat(result.getTotalPoints()).isZero();
        assertThat(result.getLevel()).isEqualTo(1);

        verify(userRepository).findByUserId(externalId);
        verify(userRepository).save(argThat(u ->
                u.getUserId().equals(externalId) &&
                        u.getTotalPoints() == 0 &&
                        u.getLevel() == 1
        ));
    }

    @Test
    void get_existingUser_shouldReturnUser() {
        String externalId = "user-ok";
        User user = User.builder().userId(externalId).build();

        when(userRepository.findByUserId(externalId)).thenReturn(Optional.of(user));

        User result = userService.get(externalId);

        assertThat(result).isSameAs(user);
    }

    @Test
    void get_nonExistingUser_shouldThrowException() {
        when(userRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.get("unknown"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("unknown");
    }
}