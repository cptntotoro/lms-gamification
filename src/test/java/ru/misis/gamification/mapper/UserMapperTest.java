package ru.misis.gamification.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.dto.user.response.UserDto;
import ru.misis.gamification.dto.user.response.UserStatisticsDto;
import ru.misis.gamification.model.UserAdminView;
import ru.misis.gamification.model.UserProgressView;
import ru.misis.gamification.model.UserStatisticsView;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toUserDto_mapsAllFieldsCorrectly() {
        UserProgressView view = new UserProgressView(
                "user-abc123",
                3420,
                12,
                580L,
                74.1
        );

        UserDto dto = mapper.toUserDto(view);

        assertThat(dto).isNotNull();
        assertThat(dto.getUserId()).isEqualTo("user-abc123");
        assertThat(dto.getTotalPoints()).isEqualTo(3420);
        assertThat(dto.getLevel()).isEqualTo(12);
        assertThat(dto.getPointsToNextLevel()).isEqualTo(580L);
        assertThat(dto.getProgressPercent()).isEqualTo(74.1);
    }

    @Test
    void toUserDto_nullView_returnsNull() {
        assertThat(mapper.toUserDto(null)).isNull();
    }

    @Test
    void toUserAdminDto_mapsAllFieldsCorrectly() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime created = LocalDateTime.of(2026, 2, 10, 14, 30);
        LocalDateTime updated = LocalDateTime.of(2026, 2, 15, 9, 45);

        UserAdminView view = new UserAdminView(
                uuid,
                "admin-user-999",
                1250,
                7,
                750L,
                62.5,
                created,
                updated
        );

        UserAdminDto dto = mapper.toUserAdminDto(view);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getUserId()).isEqualTo("admin-user-999");
        assertThat(dto.getTotalPoints()).isEqualTo(1250);
        assertThat(dto.getLevel()).isEqualTo(7);
        assertThat(dto.getPointsToNextLevel()).isEqualTo(750L);
        assertThat(dto.getProgressPercent()).isEqualTo(62.5);
        assertThat(dto.getCreatedAt()).isEqualTo(created);
        assertThat(dto.getUpdatedAt()).isEqualTo(updated);
    }

    @Test
    void toUserAdminDto_nullView_returnsNull() {
        assertThat(mapper.toUserAdminDto(null)).isNull();
    }

    @Test
    void toUserStatisticsDto_mapsAllFieldsCorrectly() {
        UserStatisticsView view = new UserStatisticsView(
                "stud-98765",
                12,
                3420,
                "CS-101-2025",
                "G-14",
                1450,
                18L,
                4L,
                580L,
                74.1
        );

        UserStatisticsDto dto = mapper.toUserStatisticsDto(view);

        assertThat(dto).isNotNull();
        assertThat(dto.getUserId()).isEqualTo("stud-98765");
        assertThat(dto.getGlobalLevel()).isEqualTo(12);
        assertThat(dto.getTotalPoints()).isEqualTo(3420);
        assertThat(dto.getCourseId()).isEqualTo("CS-101-2025");
        assertThat(dto.getGroupId()).isEqualTo("G-14");
        assertThat(dto.getPointsInCourse()).isEqualTo(1450);
        assertThat(dto.getRankInCourse()).isEqualTo(18L);
        assertThat(dto.getRankInGroup()).isEqualTo(4L);
        assertThat(dto.getPointsToNextGlobalLevel()).isEqualTo(580L);
        assertThat(dto.getProgressPercent()).isEqualTo(74.1);
    }

    @Test
    void toUserStatisticsDto_nullView_returnsNull() {
        assertThat(mapper.toUserStatisticsDto(null)).isNull();
    }

    @Test
    void toUserStatisticsDto_nullGroupId_mapsCorrectly() {
        UserStatisticsView view = new UserStatisticsView(
                "stud-555",
                8,
                2100,
                "MATH-2026",
                null,           // без группы
                900,
                42L,
                null,
                400L,
                69.0
        );

        UserStatisticsDto dto = mapper.toUserStatisticsDto(view);

        assertThat(dto.getGroupId()).isNull();
        assertThat(dto.getRankInGroup()).isNull();
        assertThat(dto.getCourseId()).isEqualTo("MATH-2026");
        assertThat(dto.getPointsInCourse()).isEqualTo(900);
    }
}