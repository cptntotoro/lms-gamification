package ru.misis.gamification.service.analytics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

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

    @Test
    void getGroupLeaderboard_validInput_returnsLeaderboardPage() {
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

        Page<LeaderboardEntryDto> mockPage = new PageImpl<>(List.of(entry1, entry2), PageRequest.of(0, 10), 2);

        when(courseService.existsByCourseId(validCourseId)).thenReturn(true);
        when(groupService.existsByGroupIdAndCourseId(validGroupId, validCourseId)).thenReturn(true);
        when(enrollmentRepository.findLeaderboardByCourseAndGroup(
                eq(validCourseId),
                eq(validGroupId),
                any(Pageable.class)
        )).thenReturn(mockPage);

        GroupLeaderboardPageDto result = analyticsService.getGroupLeaderboard(
                validCourseId, validGroupId, 0, 10
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2L);
        assertThat(result.getPageNumber()).isZero();
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getContent().getFirst().getUserId()).isEqualTo("alice");
        assertThat(result.getContent().getFirst().getRank()).isEqualTo(1L);

        verify(courseService).existsByCourseId(validCourseId);
        verify(groupService).existsByGroupIdAndCourseId(validGroupId, validCourseId);
        verify(enrollmentRepository).findLeaderboardByCourseAndGroup(
                eq(validCourseId), eq(validGroupId), any(Pageable.class)
        );
        verifyNoMoreInteractions(courseService, groupService, enrollmentRepository);
    }

    @Test
    void getGroupLeaderboard_nullCourseId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> analyticsService.getGroupLeaderboard(null, validGroupId, 0, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Идентификатор курса не может быть пустым или null");

        verifyNoInteractions(courseService, groupService, enrollmentRepository);
    }

    @Test
    void getGroupLeaderboard_emptyCourseId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> analyticsService.getGroupLeaderboard("   ", validGroupId, 0, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Идентификатор курса не может быть пустым или null");

        verifyNoInteractions(courseService, groupService, enrollmentRepository);
    }

    @Test
    void getGroupLeaderboard_nullGroupId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> analyticsService.getGroupLeaderboard(validCourseId, null, 0, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Идентификатор группы не может быть пустым или null");

        verifyNoInteractions(courseService, groupService, enrollmentRepository);
    }

    @Test
    void getGroupLeaderboard_courseNotFound_throwsCourseNotFoundException() {
        when(courseService.existsByCourseId(validCourseId)).thenReturn(false);

        assertThatThrownBy(() -> analyticsService.getGroupLeaderboard(validCourseId, validGroupId, 0, 10))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessageContaining(validCourseId);

        verify(courseService).existsByCourseId(validCourseId);
        verifyNoMoreInteractions(courseService, groupService, enrollmentRepository);
    }

    @Test
    void getGroupLeaderboard_groupNotFound_throwsGroupNotFoundException() {
        when(courseService.existsByCourseId(validCourseId)).thenReturn(true);
        when(groupService.existsByGroupIdAndCourseId(validGroupId, validCourseId)).thenReturn(false);

        assertThatThrownBy(() -> analyticsService.getGroupLeaderboard(validCourseId, validGroupId, 0, 10))
                .isInstanceOf(GroupNotFoundException.class)
                .hasMessageContaining(validGroupId)
                .hasMessageContaining(validCourseId);

        verify(courseService).existsByCourseId(validCourseId);
        verify(groupService).existsByGroupIdAndCourseId(validGroupId, validCourseId);
        verifyNoInteractions(enrollmentRepository);
    }

    @Test
    void getGroupLeaderboard_emptyResult_returnsEmptyPage() {
        when(courseService.existsByCourseId(validCourseId)).thenReturn(true);
        when(groupService.existsByGroupIdAndCourseId(validGroupId, validCourseId)).thenReturn(true);

        Page<LeaderboardEntryDto> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(enrollmentRepository.findLeaderboardByCourseAndGroup(
                eq(validCourseId),
                eq(validGroupId),
                any(Pageable.class)
        )).thenReturn(emptyPage);

        GroupLeaderboardPageDto result = analyticsService.getGroupLeaderboard(validCourseId, validGroupId, 0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.isHasPrevious()).isFalse();

        verify(enrollmentRepository).findLeaderboardByCourseAndGroup(
                eq(validCourseId), eq(validGroupId), any(Pageable.class)
        );
    }

    @Test
    void getGroupLeaderboard_secondPage_returnsCorrectPaginationMetadata() {
        LeaderboardEntryDto entry = LeaderboardEntryDto.builder()
                .userUuid(UUID.randomUUID())
                .userId("carol")
                .pointsInCourse(300)
                .globalLevel(3)
                .rank(11L)
                .build();

        when(courseService.existsByCourseId(validCourseId)).thenReturn(true);
        when(groupService.existsByGroupIdAndCourseId(validGroupId, validCourseId)).thenReturn(true);

        Page<LeaderboardEntryDto> mockPage = Mockito.mock(Page.class);
        when(mockPage.getContent()).thenReturn(List.of(entry));
        when(mockPage.getNumber()).thenReturn(1);
        when(mockPage.getSize()).thenReturn(10);
        when(mockPage.getTotalElements()).thenReturn(15L);
        when(mockPage.getTotalPages()).thenReturn(2);
        when(mockPage.hasNext()).thenReturn(false);
        when(mockPage.hasPrevious()).thenReturn(true);

        when(enrollmentRepository.findLeaderboardByCourseAndGroup(
                anyString(), anyString(), any(Pageable.class)
        )).thenReturn(mockPage);

        GroupLeaderboardPageDto result = analyticsService.getGroupLeaderboard(validCourseId, validGroupId, 1, 10);

        assertThat(result.getTotalElements()).isEqualTo(15L);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(enrollmentRepository).findLeaderboardByCourseAndGroup(
                eq(validCourseId),
                eq(validGroupId),
                pageableCaptor.capture()
        );

        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageNumber()).isEqualTo(1);
        assertThat(capturedPageable.getPageSize()).isEqualTo(10);
        assertThat(capturedPageable.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "totalPointsInCourse"));
    }
}