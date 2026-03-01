package ru.misis.gamification.service.analytics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.misis.gamification.dto.analytics.GroupLeaderboardPageDto;
import ru.misis.gamification.dto.analytics.LeaderboardEntryDto;
import ru.misis.gamification.exception.CourseNotFoundException;
import ru.misis.gamification.exception.GroupNotFoundException;
import ru.misis.gamification.repository.UserCourseEnrollmentRepository;
import ru.misis.gamification.service.course.CourseService;
import ru.misis.gamification.service.group.GroupService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceUnitTest {

    @Mock
    private UserCourseEnrollmentRepository enrollmentRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private GroupService groupService;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private final String validCourseId = "MATH-101";
    private final String validGroupId = "PM-21-1";
    private final UUID courseUuid = UUID.randomUUID();
    private final UUID groupUuid = UUID.randomUUID();

    @Test
    void getGroupLeaderboard_validInput_returnsCorrectLeaderboardPage() {
        LeaderboardEntryDto entry1 = LeaderboardEntryDto.builder()
                .userUuid(UUID.randomUUID())
                .userId("alice")
                .pointsInCourse(850)
                .globalLevel(5)
                .rank(1L)
                .build();

        LeaderboardEntryDto entry2 = LeaderboardEntryDto.builder()
                .userUuid(UUID.randomUUID())
                .userId("bob")
                .pointsInCourse(620)
                .globalLevel(4)
                .rank(2L)
                .build();

        Page<LeaderboardEntryDto> mockPage = new PageImpl<>(
                List.of(entry1, entry2),
                PageRequest.of(0, 10, Sort.by("totalPointsInCourse").descending()),
                2
        );

        when(courseService.getCourseUuidByExternalId(validCourseId)).thenReturn(courseUuid);
        when(groupService.getGroupUuidByExternalIdAndCourseId(validGroupId, validCourseId)).thenReturn(groupUuid);
        when(enrollmentRepository.findLeaderboardByCourseAndGroup(
                eq(courseUuid),
                eq(groupUuid),
                any(Pageable.class)
        )).thenReturn(mockPage);

        GroupLeaderboardPageDto result = analyticsService.getGroupLeaderboard(validCourseId, validGroupId, 0, 10);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getPageNumber()).isZero();
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getContent().getFirst().getUserId()).isEqualTo("alice");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(enrollmentRepository).findLeaderboardByCourseAndGroup(
                eq(courseUuid),
                eq(groupUuid),
                captor.capture()
        );

        Pageable captured = captor.getValue();
        assertThat(captured.getSort().toString()).contains("totalPointsInCourse: DESC");
    }

    @Test
    void getGroupLeaderboard_courseNotFound_throwsCourseNotFoundException() {
        when(courseService.getCourseUuidByExternalId(validCourseId))
                .thenThrow(new CourseNotFoundException(validCourseId));

        assertThatThrownBy(() -> analyticsService.getGroupLeaderboard(validCourseId, validGroupId, 0, 10))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessageContaining(validCourseId);

        verify(courseService).getCourseUuidByExternalId(validCourseId);
        verifyNoMoreInteractions(groupService, enrollmentRepository);
    }

    @Test
    void getGroupLeaderboard_groupNotFound_throwsGroupNotFoundException() {
        when(courseService.getCourseUuidByExternalId(validCourseId)).thenReturn(courseUuid);
        when(groupService.getGroupUuidByExternalIdAndCourseId(validGroupId, validCourseId))
                .thenThrow(new GroupNotFoundException(validGroupId, validCourseId));

        assertThatThrownBy(() -> analyticsService.getGroupLeaderboard(validCourseId, validGroupId, 0, 10))
                .isInstanceOf(GroupNotFoundException.class)
                .hasMessageContaining(validGroupId);

        verify(courseService).getCourseUuidByExternalId(validCourseId);
        verify(groupService).getGroupUuidByExternalIdAndCourseId(validGroupId, validCourseId);
        verifyNoMoreInteractions(enrollmentRepository);
    }

    @Test
    void getGroupLeaderboard_emptyResult_returnsEmptyPageDto() {
        when(courseService.getCourseUuidByExternalId(validCourseId)).thenReturn(courseUuid);
        when(groupService.getGroupUuidByExternalIdAndCourseId(validGroupId, validCourseId)).thenReturn(groupUuid);

        Page<LeaderboardEntryDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(enrollmentRepository.findLeaderboardByCourseAndGroup(any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        GroupLeaderboardPageDto result = analyticsService.getGroupLeaderboard(validCourseId, validGroupId, 0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.isHasPrevious()).isFalse();
    }
}