package ru.misis.gamification.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.mapper.UserMapper;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserAdminServiceImpl userAdminService;

    private User testUser;
    private UserAdminDto testUserAdminDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .uuid(UUID.randomUUID())
                .userId("lms-user-12345")
                .totalPoints(1250)
                .level(3)
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now())
                .build();

        testUserAdminDto = UserAdminDto.builder()
                .uuid(testUser.getUuid())
                .userId(testUser.getUserId())
                .totalPoints(testUser.getTotalPoints())
                .level(testUser.getLevel())
                .createdAt(testUser.getCreatedAt())
                .updatedAt(testUser.getUpdatedAt())
                .build();
    }

    @Test
    void findByUserId_existingUser_shouldReturnUserAdminDto() {
        String userId = "lms-user-12345";

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.userToUserAdminDto(testUser)).thenReturn(testUserAdminDto);

        UserAdminDto result = userAdminService.findByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTotalPoints()).isEqualTo(1250);
        assertThat(result.getLevel()).isEqualTo(3);

        verify(userRepository).findByUserId(userId);
        verify(userMapper).userToUserAdminDto(testUser);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    void findByUserId_nonExistingUser_shouldThrowUserNotFoundException() {
        String userId = "unknown-user";

        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAdminService.findByUserId(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID " + userId + " не найден");

        verify(userRepository).findByUserId(userId);
        verifyNoInteractions(userMapper);
    }

    @Test
    void findByUserId_blankUserId_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> userAdminService.findByUserId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть пустым");

        assertThatThrownBy(() -> userAdminService.findByUserId("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть пустым");

        verifyNoInteractions(userRepository, userMapper);
    }

    @Test
    void findAll_shouldReturnPageOfUserAdminDtos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.userToUserAdminDto(testUser)).thenReturn(testUserAdminDto);

        Page<UserAdminDto> result = userAdminService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getUserId()).isEqualTo("lms-user-12345");

        verify(userRepository).findAll(pageable);
        verify(userMapper).userToUserAdminDto(testUser);
        verifyNoMoreInteractions(userRepository, userMapper);
    }
}