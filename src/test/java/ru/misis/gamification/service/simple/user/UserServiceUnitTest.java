package ru.misis.gamification.service.simple.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.repository.UserRepository;
import ru.misis.gamification.service.application.enrollment.EnrollmentApplicationService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentApplicationService enrollmentApplicationService;

    @Spy
    @InjectMocks
    private UserServiceImpl service;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User existingUser;
    private UUID uuid;

    @BeforeEach
    void setUp() throws Exception {
        uuid = UUID.randomUUID();
        existingUser = User.builder()
                .uuid(uuid)
                .userId("user-123")
                .totalPoints(500)
                .level(3)
                .build();

        Field initialPointsField = UserServiceImpl.class.getDeclaredField("initialPoints");
        initialPointsField.setAccessible(true);
        initialPointsField.set(service, 0);

        Field initialLevelField = UserServiceImpl.class.getDeclaredField("initialLevel");
        initialLevelField.setAccessible(true);
        initialLevelField.set(service, 1);
    }

    @Test
    void createIfNotExists_userExists_returnsExisting() {
        when(userRepository.findByUserIdWithLock("user-123")).thenReturn(Optional.of(existingUser));

        User result = service.createIfNotExists("user-123");

        assertThat(result).isSameAs(existingUser);
        verify(userRepository).findByUserIdWithLock("user-123");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(enrollmentApplicationService);
    }

    @Test
    void createIfNotExists_userNotExists_createsAndEnrolls() {
        when(userRepository.findByUserIdWithLock("new-user")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUuid(UUID.randomUUID());
            return u;
        });

        User result = service.createIfNotExists("new-user", "CS-101", "G-14");

        assertThat(result.getUserId()).isEqualTo("new-user");
        assertThat(result.getTotalPoints()).isZero();
        assertThat(result.getLevel()).isEqualTo(1);
        assertThat(result.getUuid()).isNotNull();

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo("new-user");

        verify(enrollmentApplicationService).enrollIfNeeded("new-user", "CS-101", "G-14");
    }

    @Test
    void get_existingUser_returnsUser() {
        when(userRepository.findByUserId("user-123")).thenReturn(Optional.of(existingUser));

        User result = service.get("user-123");

        assertThat(result).isSameAs(existingUser);
    }

    @Test
    void get_notFound_throwsUserNotFoundException() {
        when(userRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get("unknown"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void update_validUser_savesAndReturnsUpdated() {
        User toUpdate = new User(
                existingUser.getUuid(),
                existingUser.getUserId(),
                1000,
                5,
                existingUser.getCreatedAt(),
                existingUser.getUpdatedAt()
        );

        when(userRepository.save(toUpdate)).thenReturn(toUpdate);

        User result = service.update(toUpdate);

        assertThat(result).isSameAs(toUpdate);
        verify(userRepository).save(toUpdate);
    }

    @Test
    void update_userWithoutUuid_throwsIllegalArgument() {
        User withoutUuid = User.builder()
                .userId("user-123")
                .totalPoints(100)
                .build();

        assertThatThrownBy(() -> service.update(withoutUuid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uuid");
    }

    @Test
    void findAll_callsRepositoryWithPageable() {
        Pageable pageable = PageRequest.of(1, 20);
        Page<User> page = new PageImpl<>(List.of(existingUser), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = service.findAll(pageable);

        assertThat(result).isSameAs(page);
        verify(userRepository).findAll(pageable);
    }

    @Test
    void getUserUuidByExternalId_existing_returnsUuid() {
        when(userRepository.findUuidByUserId("user-123")).thenReturn(Optional.of(uuid));

        UUID result = service.getUserUuidByExternalId("user-123");

        assertThat(result).isEqualTo(uuid);
    }

    @Test
    void getUserUuidByExternalId_notFound_throwsException() {
        when(userRepository.findUuidByUserId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserUuidByExternalId("unknown"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void getUserByExternalId_existing_returnsUser() {
        when(userRepository.findByUserId("user-123")).thenReturn(Optional.of(existingUser));

        User result = service.getUserByExternalId("user-123");

        assertThat(result).isSameAs(existingUser);
    }

    @Test
    void getUserByExternalId_notFound_throwsException() {
        when(userRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserByExternalId("unknown"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getByUuid_existing_returnsUser() {
        when(userRepository.findById(uuid)).thenReturn(Optional.of(existingUser));

        User result = service.getByUuid(uuid);

        assertThat(result).isSameAs(existingUser);
    }

    @Test
    void getByUuid_notFound_throwsException() {
        UUID unknownUuid = UUID.randomUUID();
        when(userRepository.findById(unknownUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByUuid(unknownUuid))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(unknownUuid.toString());
    }
}