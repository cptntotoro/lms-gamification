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

import java.time.LocalDateTime;
import java.util.List;
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
    private UserService userService;

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

        when(userService.get(userId)).thenReturn(testUser);
        when(userMapper.userToUserAdminDto(testUser)).thenReturn(testUserAdminDto);

        UserAdminDto result = userAdminService.findByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTotalPoints()).isEqualTo(1250);
        assertThat(result.getLevel()).isEqualTo(3);

        verify(userService).get(userId);
        verify(userMapper).userToUserAdminDto(testUser);
        verifyNoMoreInteractions(userService, userMapper);
    }

    @Test
    void findByUserId_nonExistingUser_shouldThrowUserNotFoundException() {
        String userId = "unknown-user";

        when(userService.get(userId)).thenThrow(new UserNotFoundException("Пользователь с ID " + userId + " не найден"));

        assertThatThrownBy(() -> userAdminService.findByUserId(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID " + userId + " не найден");

        verify(userService).get(userId);
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

        verifyNoInteractions(userService, userMapper);
    }

    @Test
    void findAll_shouldReturnPageOfUserAdminDtos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userService.findAll(pageable)).thenReturn(userPage);
        when(userMapper.userToUserAdminDto(testUser)).thenReturn(testUserAdminDto);

        Page<UserAdminDto> result = userAdminService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getUserId()).isEqualTo("lms-user-12345");

        verify(userService).findAll(pageable);
        verify(userMapper).userToUserAdminDto(testUser);
        verifyNoMoreInteractions(userService, userMapper);
    }

    @Test
    void findAll_emptyPage_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userService.findAll(pageable)).thenReturn(emptyPage);

        Page<UserAdminDto> result = userAdminService.findAll(pageable);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();

        verify(userService).findAll(pageable);
        verifyNoInteractions(userMapper);
    }
}