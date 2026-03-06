package ru.misis.gamification.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.misis.gamification.dto.analytics.GroupLeaderboardPageDto;
import ru.misis.gamification.dto.analytics.LeaderboardEntryDto;
import ru.misis.gamification.dto.analytics.UserCourseGroupLeaderboardDto;
import ru.misis.gamification.model.LeaderboardEntryView;
import ru.misis.gamification.model.LeaderboardPageView;
import ru.misis.gamification.model.UserCourseGroupLeaderboardView;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LeaderboardMapperTest {

    private final LeaderboardMapper mapper = Mappers.getMapper(LeaderboardMapper.class);

    // ────────────────────────────────────────────────────────────────
    // toLeaderboardEntryDto (LeaderboardEntryView → LeaderboardEntryDto)
    // ────────────────────────────────────────────────────────────────

    @Test
    void toLeaderboardEntryDto_mapsAllFieldsCorrectly() {
        UUID uuid = UUID.randomUUID();

        LeaderboardEntryView view = new LeaderboardEntryView(
                uuid,
                "user-abc123",
                850,
                5,
                3L,
                true
        );

        LeaderboardEntryDto dto = mapper.toLeaderboardEntryDto(view);

        assertThat(dto).isNotNull();
        assertThat(dto.getUserUuid()).isEqualTo(uuid);
        assertThat(dto.getUserId()).isEqualTo("user-abc123");
        assertThat(dto.getPointsInCourse()).isEqualTo(850);
        assertThat(dto.getGlobalLevel()).isEqualTo(5);
        assertThat(dto.getRank()).isEqualTo(3L);
        assertThat(dto.getIsCurrentUser()).isTrue();
    }

    @Test
    void toLeaderboardEntryDto_defaultIsCurrentUserFalse() {
        UUID uuid = UUID.randomUUID();

        LeaderboardEntryView view = new LeaderboardEntryView(
                uuid,
                "user-xyz",
                420,
                4,
                7L
        );  // без явного isCurrentUser → false

        LeaderboardEntryDto dto = mapper.toLeaderboardEntryDto(view);

        assertThat(dto).isNotNull();
        assertThat(dto.getIsCurrentUser()).isFalse();
    }

    @Test
    void toLeaderboardEntryDto_nullView_returnsNull() {
        assertThat(mapper.toLeaderboardEntryDto(null)).isNull();
    }

    // ────────────────────────────────────────────────────────────────
    // toGroupLeaderboardPageDto (LeaderboardPageView → GroupLeaderboardPageDto)
    // ────────────────────────────────────────────────────────────────

    @Test
    void toGroupLeaderboardPageDto_mapsPageCorrectly() {
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();

        List<LeaderboardEntryView> entries = List.of(
                new LeaderboardEntryView(u1, "alice", 1000, 6, 1L, false),
                new LeaderboardEntryView(u2, "bob", 800, 5, 2L, false)
        );

        LeaderboardPageView view = new LeaderboardPageView(
                entries,
                0,
                20,
                45L,
                3,
                true,
                false
        );

        GroupLeaderboardPageDto dto = mapper.toGroupLeaderboardPageDto(view);

        assertThat(dto).isNotNull();
        assertThat(dto.getContent()).hasSize(2);
        assertThat(dto.getContent().get(0).getUserId()).isEqualTo("alice");
        assertThat(dto.getContent().get(0).getRank()).isEqualTo(1L);
        assertThat(dto.getContent().get(1).getUserId()).isEqualTo("bob");
        assertThat(dto.getContent().get(1).getRank()).isEqualTo(2L);

        assertThat(dto.getPageNumber()).isZero();
        assertThat(dto.getPageSize()).isEqualTo(20);
        assertThat(dto.getTotalElements()).isEqualTo(45L);
        assertThat(dto.getTotalPages()).isEqualTo(3);
        assertThat(dto.isHasNext()).isTrue();
        assertThat(dto.isHasPrevious()).isFalse();
    }

    @Test
    void toGroupLeaderboardPageDto_emptyPage_mapsCorrectly() {
        LeaderboardPageView view = new LeaderboardPageView(
                List.of(),
                2,
                10,
                0L,
                0,
                false,
                true
        );

        GroupLeaderboardPageDto dto = mapper.toGroupLeaderboardPageDto(view);

        assertThat(dto).isNotNull();
        assertThat(dto.getContent()).isEmpty();
        assertThat(dto.getPageNumber()).isEqualTo(2);
        assertThat(dto.getPageSize()).isEqualTo(10);
        assertThat(dto.getTotalElements()).isZero();
        assertThat(dto.getTotalPages()).isZero();
        assertThat(dto.isHasNext()).isFalse();
        assertThat(dto.isHasPrevious()).isTrue();
    }

    @Test
    void toGroupLeaderboardPageDto_nullView_returnsNull() {
        assertThat(mapper.toGroupLeaderboardPageDto(null)).isNull();
    }

    // ────────────────────────────────────────────────────────────────
    // toUserCourseGroupLeaderboardDto (UserCourseGroupLeaderboardView → UserCourseGroupLeaderboardDto)
    // ────────────────────────────────────────────────────────────────

    @Test
    void toUserCourseGroupLeaderboardDto_mapsAllFieldsCorrectly() {
        UUID u1 = UUID.randomUUID();
        UUID currentUuid = UUID.randomUUID();

        List<LeaderboardEntryView> top = List.of(
                new LeaderboardEntryView(u1, "alice", 1200, 7, 1L, false)
        );

        LeaderboardEntryView current = new LeaderboardEntryView(
                currentUuid,
                "current-user",
                950,
                6,
                4L,
                true
        );

        UserCourseGroupLeaderboardView view = new UserCourseGroupLeaderboardView(
                top,
                current,
                4L,
                950,
                0,
                20,
                50L,
                3,
                true,
                false
        );

        UserCourseGroupLeaderboardDto dto = mapper.toUserCourseGroupLeaderboardDto(view);

        assertThat(dto).isNotNull();

        // Топ
        assertThat(dto.getTopEntries()).hasSize(1);
        assertThat(dto.getTopEntries().getFirst().getUserId()).isEqualTo("alice");
        assertThat(dto.getTopEntries().getFirst().getRank()).isEqualTo(1L);

        // Текущий пользователь
        assertThat(dto.getCurrentUserEntry()).isNotNull();
        assertThat(dto.getCurrentUserEntry().getUserId()).isEqualTo("current-user");
        assertThat(dto.getCurrentUserEntry().getPointsInCourse()).isEqualTo(950);
        assertThat(dto.getCurrentUserEntry().getIsCurrentUser()).isTrue();

        // Ранг и очки
        assertThat(dto.getCurrentUserRank()).isEqualTo(4L);
        assertThat(dto.getCurrentUserPoints()).isEqualTo(950);

        // Пагинация
        assertThat(dto.getPageNumber()).isZero();
        assertThat(dto.getPageSize()).isEqualTo(20);
        assertThat(dto.getTotalElements()).isEqualTo(50L);
        assertThat(dto.getTotalPages()).isEqualTo(3);
        assertThat(dto.isHasNext()).isTrue();
        assertThat(dto.isHasPrevious()).isFalse();
    }

    @Test
    void toUserCourseGroupLeaderboardDto_withoutCurrentUser_mapsCorrectly() {
        List<LeaderboardEntryView> top = List.of(
                new LeaderboardEntryView(UUID.randomUUID(), "alice", 1000, 6, 1L, false)
        );

        UserCourseGroupLeaderboardView view = new UserCourseGroupLeaderboardView(
                top,
                null,
                null,
                null,
                1,
                15,
                30L,
                2,
                false,
                true
        );

        UserCourseGroupLeaderboardDto dto = mapper.toUserCourseGroupLeaderboardDto(view);

        assertThat(dto).isNotNull();
        assertThat(dto.getCurrentUserEntry()).isNull();
        assertThat(dto.getCurrentUserRank()).isNull();
        assertThat(dto.getCurrentUserPoints()).isNull();
        assertThat(dto.getTopEntries()).hasSize(1);
        assertThat(dto.getPageNumber()).isEqualTo(1);
        assertThat(dto.isHasPrevious()).isTrue();
        assertThat(dto.isHasNext()).isFalse();
    }

    @Test
    void toUserCourseGroupLeaderboardDto_nullView_returnsNull() {
        assertThat(mapper.toUserCourseGroupLeaderboardDto(null)).isNull();
    }
}