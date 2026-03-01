package ru.misis.gamification.service.user;

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
import ru.misis.gamification.entity.User;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.repository.UserRepository;
import ru.misis.gamification.service.course.UserCourseService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCourseService userCourseService;

    @InjectMocks
    private UserServiceImpl service;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private final String userId = "user-123";
    private final UUID userUuid = UUID.randomUUID();

    @Test
    void createIfNotExists_userAlreadyExists_returnsExisting() {
        User existing = User.builder()
                .uuid(userUuid)
                .userId(userId)
                .totalPoints(500)
                .level(3)
                .build();

        when(userRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(existing));

        User result = service.createIfNotExists(userId);

        assertThat(result).isSameAs(existing);
        verify(userRepository).findByUserIdWithLock(userId);
        verify(userRepository, never()).save(any());
        verify(userCourseService, never()).enrollIfNeeded(any(), any(), any());
    }

    @Test
    void createIfNotExists_userNotExists_createsAndSavesNew() {
        when(userRepository.findByUserIdWithLock(userId)).thenReturn(Optional.empty());

        User saved = User.builder()
                .uuid(userUuid)
                .userId(userId)
                .totalPoints(0)
                .level(1)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = service.createIfNotExists(userId);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTotalPoints()).isZero();
        assertThat(result.getLevel()).isOne();

        verify(userRepository).findByUserIdWithLock(userId);
        verify(userRepository).save(userCaptor.capture());
        User captured = userCaptor.getValue();
        assertThat(captured.getUserId()).isEqualTo(userId);
        assertThat(captured.getTotalPoints()).isZero();
        assertThat(captured.getLevel()).isOne();

        verify(userCourseService).enrollIfNeeded(saved, null, null);
    }

    @Test
    void createIfNotExists_withCourseAndGroup_enrollsUser() {
        String courseId = "MATH-101";
        String groupId = "PM-21-1";

        when(userRepository.findByUserIdWithLock(userId)).thenReturn(Optional.empty());

        User saved = User.builder()
                .uuid(userUuid)
                .userId(userId)
                .totalPoints(0)
                .level(1)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = service.createIfNotExists(userId, courseId, groupId);

        assertThat(result.getUserId()).isEqualTo(userId);

        verify(userCourseService).enrollIfNeeded(saved, courseId, groupId);
    }

    @Test
    void get_existingUser_returnsUser() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(User.builder().uuid(userUuid).userId(userId).build()));

        User result = service.get(userId);

        assertThat(result.getUserId()).isEqualTo(userId);
        verify(userRepository).findByUserId(userId);
    }

    @Test
    void get_nonExistingUser_throwsUserNotFoundException() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId);

        verify(userRepository).findByUserId(userId);
    }

    @Test
    void getUserByExternalId_existing_returnsUser() {
        when(userRepository.findUuidByUserId(userId)).thenReturn(Optional.of(userUuid));
        when(userRepository.findById(userUuid)).thenReturn(Optional.of(User.builder().uuid(userUuid).userId(userId).build()));

        User result = service.getUserByExternalId(userId);

        assertThat(result.getUserId()).isEqualTo(userId);
        verify(userRepository).findUuidByUserId(userId);
        verify(userRepository).findById(userUuid);
    }

    @Test
    void getUserByExternalId_notFound_throwsUserNotFoundException() {
        when(userRepository.findUuidByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserByExternalId(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findUuidByUserId(userId);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getByUuid_existing_returnsUser() {
        when(userRepository.findById(userUuid)).thenReturn(Optional.of(User.builder().uuid(userUuid).build()));

        User result = service.getByUuid(userUuid);

        assertThat(result.getUuid()).isEqualTo(userUuid);
        verify(userRepository).findById(userUuid);
    }

    @Test
    void getByUuid_notFound_throwsUserNotFoundException() {
        when(userRepository.findById(userUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByUuid(userUuid))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userUuid.toString());

        verify(userRepository).findById(userUuid);
    }

    @Test
    void update_validUser_savesAndReturns() {
        User toUpdate = User.builder()
                .uuid(userUuid)
                .userId(userId)
                .totalPoints(500)
                .level(5)
                .build();

        User saved = User.builder()
                .uuid(userUuid)
                .userId(userId)
                .totalPoints(550)
                .level(6)
                .build();

        when(userRepository.save(toUpdate)).thenReturn(saved);

        User result = service.update(toUpdate);

        assertThat(result.getTotalPoints()).isEqualTo(550);
        assertThat(result.getLevel()).isEqualTo(6);
        assertThat(result.getUuid()).isEqualTo(userUuid);
        assertThat(result.getUserId()).isEqualTo(userId);

        verify(userRepository).save(toUpdate);
    }

    @Test
    void update_noUuid_throwsIllegalArgumentException() {
        User invalid = User.builder()
                .userId(userId)
                .totalPoints(300)
                .build();

        assertThatThrownBy(() -> service.update(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Пользователь должен иметь uuid для обновления");

        verify(userRepository, never()).save(any());
    }

    @Test
    void findAll_withData_returnsPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(
                User.builder().uuid(UUID.randomUUID()).userId("user-1").build(),
                User.builder().uuid(UUID.randomUUID()).userId("user-2").build()
        );
        Page<User> userPage = new PageImpl<>(users, pageable, 2);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<User> result = service.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);

        verify(userRepository).findAll(pageable);
    }

    @Test
    void findAll_emptyRepository_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<User> result = service.findAll(pageable);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();

        verify(userRepository).findAll(pageable);
    }
}