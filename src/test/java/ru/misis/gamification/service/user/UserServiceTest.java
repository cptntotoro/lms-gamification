package ru.misis.gamification.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.repository.UserRepository;
import ru.misis.gamification.service.course.UserCourseServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCourseServiceImpl userCourseService;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

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
        verify(userCourseService, never()).enrollIfNeeded(any(), any(), any());
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
        verify(userRepository).save(userCaptor.capture());
        User captured = userCaptor.getValue();
        assertThat(captured.getUserId()).isEqualTo(newUserId);
        assertThat(captured.getTotalPoints()).isZero();
        assertThat(captured.getLevel()).isEqualTo(1);

        verify(userCourseService).enrollIfNeeded(savedUser, null, null);
    }

    @Test
    void createIfNotExists_withCourseAndGroup_shouldEnroll() {
        String newUserId = "new-user-999";
        String courseId = "MATH-101";
        String groupId = "1-A";

        when(userRepository.findByUserIdWithLock(newUserId)).thenReturn(Optional.empty());

        User savedUser = User.builder()
                .uuid(UUID.randomUUID())
                .userId(newUserId)
                .totalPoints(0)
                .level(1)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createIfNotExists(newUserId, courseId, groupId);

        assertThat(result.getUserId()).isEqualTo(newUserId);

        verify(userCourseService).enrollIfNeeded(savedUser, courseId, groupId);
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
        verify(userCourseService, never()).enrollIfNeeded(any(), any(), any());
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
        verify(userCourseService).enrollIfNeeded(savedUser, null, null);
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

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findAll_withData_shouldReturnPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 5);
        List<User> users = List.of(
                User.builder().uuid(UUID.randomUUID()).userId("user-1").totalPoints(100).level(2).build(),
                User.builder().uuid(UUID.randomUUID()).userId("user-2").totalPoints(500).level(5).build()
        );
        Page<User> userPage = new PageImpl<>(users, pageable, 2);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<User> result = userService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo("user-1");
        assertThat(result.getContent().get(1).getUserId()).isEqualTo("user-2");

        verify(userRepository).findAll(pageable);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findAll_emptyRepository_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<User> result = userService.findAll(pageable);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalPages()).isZero();

        verify(userRepository).findAll(pageable);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findAll_secondPage_shouldReturnCorrectPage() {
        Pageable pageable = PageRequest.of(1, 5);
        List<User> usersOnPage2 = List.of(
                User.builder().uuid(UUID.randomUUID()).userId("user-6").totalPoints(300).level(4).build()
        );
        Page<User> page2 = new PageImpl<>(usersOnPage2, pageable, 6);

        when(userRepository.findAll(pageable)).thenReturn(page2);

        Page<User> result = userService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getUserId()).isEqualTo("user-6");

        verify(userRepository).findAll(pageable);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findAll_withSorting_shouldPassSortingToRepository() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("totalPoints").descending());
        Page<User> userPage = new PageImpl<>(List.of(existingUser), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<User> result = userService.findAll(pageable);

        assertThat(result.getContent().getFirst().getTotalPoints()).isEqualTo(150);

        verify(userRepository).findAll(pageable);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findAll_nullPageable_shouldThrowNullPointerException() {
        assertThatThrownBy(() -> userService.findAll(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(userRepository);
    }
}