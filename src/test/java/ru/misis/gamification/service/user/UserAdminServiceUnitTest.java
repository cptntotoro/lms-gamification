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
import ru.misis.gamification.entity.User;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserAdminServiceImpl service;

    private User testUser;
    private UserAdminDto testAdminDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .uuid(UUID.randomUUID())
                .userId("lms-user-12345")
                .totalPoints(1250)
                .level(7)
                .createdAt(LocalDateTime.of(2026, 1, 15, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 2, 20, 14, 30))
                .build();

        testAdminDto = UserAdminDto.builder()
                .uuid(testUser.getUuid())
                .userId(testUser.getUserId())
                .totalPoints(testUser.getTotalPoints())
                .level(testUser.getLevel())
                .createdAt(testUser.getCreatedAt())
                .updatedAt(testUser.getUpdatedAt())
                .build();
    }

    @Test
    void findByUserId_existingUser_returnsUserAdminDto() {
        String userId = "lms-user-12345";

        when(userService.getUserByExternalId(userId)).thenReturn(testUser);
        when(userMapper.userToUserAdminDto(testUser)).thenReturn(testAdminDto);

        UserAdminDto result = service.findByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTotalPoints()).isEqualTo(1250);
        assertThat(result.getLevel()).isEqualTo(7);
        assertThat(result.getUuid()).isEqualTo(testUser.getUuid());

        verify(userService).getUserByExternalId(userId);
        verify(userMapper).userToUserAdminDto(testUser);
    }

    @Test
    void findByUserId_nonExistingUser_throwsUserNotFoundException() {
        String userId = "unknown-user";

        when(userService.getUserByExternalId(userId))
                .thenThrow(new UserNotFoundException("Пользователь с ID " + userId + " не найден"));

        assertThatThrownBy(() -> service.findByUserId(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId);

        verify(userService).getUserByExternalId(userId);
        verifyNoInteractions(userMapper);
    }

    @Test
    void findAll_returnsPageOfUserAdminDtos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userService.findAll(pageable)).thenReturn(userPage);
        when(userMapper.userToUserAdminDto(testUser)).thenReturn(testAdminDto);

        Page<UserAdminDto> result = service.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getUserId()).isEqualTo("lms-user-12345");

        verify(userService).findAll(pageable);
        verify(userMapper).userToUserAdminDto(testUser);
    }

    @Test
    void findAll_emptyPage_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userService.findAll(pageable)).thenReturn(emptyPage);

        Page<UserAdminDto> result = service.findAll(pageable);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();

        verify(userService).findAll(pageable);
        verifyNoInteractions(userMapper);
    }
}