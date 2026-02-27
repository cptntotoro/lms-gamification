package ru.misis.gamification.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.dto.web.response.UserDto;
import ru.misis.gamification.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void userToUserAdminDto_shouldMapAllFields() {
        User user = User.builder()
                .uuid(UUID.randomUUID())
                .userId("lms-student-789")
                .totalPoints(1250)
                .level(4)
                .createdAt(LocalDateTime.of(2026, 1, 10, 9, 0))
                .updatedAt(LocalDateTime.of(2026, 2, 18, 15, 30))
                .build();

        UserAdminDto dto = mapper.userToUserAdminDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(user.getUuid());
        assertThat(dto.getUserId()).isEqualTo("lms-student-789");
        assertThat(dto.getTotalPoints()).isEqualTo(1250);
        assertThat(dto.getLevel()).isEqualTo(4);
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 10, 9, 0));
        assertThat(dto.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 2, 18, 15, 30));
    }

    @Test
    void userToUserAdminDto_nullInput_returnsNull() {
        UserAdminDto dto = mapper.userToUserAdminDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void userToUserDto_mapsWidgetFields() {
        User user = User.builder()
                .uuid(UUID.randomUUID())
                .userId("viewer-101")
                .totalPoints(950)
                .level(4)
                .build();

        UserDto dto = mapper.userToUserDto(user);

        assertThat(dto.getUserId()).isEqualTo("viewer-101");
        assertThat(dto.getTotalPoints()).isEqualTo(950);
        assertThat(dto.getLevel()).isEqualTo(4);
    }

    @Test
    void userToUserDto_nullInput_returnsNull() {
        UserDto dto = mapper.userToUserDto(null);
        assertThat(dto).isNull();
    }
}