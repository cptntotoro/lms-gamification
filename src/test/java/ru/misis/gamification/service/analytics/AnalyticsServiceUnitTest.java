package ru.misis.gamification.service.analytics;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.data.domain.Sort;
import ru.misis.gamification.dto.analytics.GroupLeaderboardPageDto;
import ru.misis.gamification.dto.analytics.LeaderboardEntryDto;
import ru.misis.gamification.dto.analytics.UserCourseGroupLeaderboard;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.CourseNotFoundException;
import ru.misis.gamification.exception.GroupNotFoundException;
import ru.misis.gamification.service.course.CourseService;
import ru.misis.gamification.service.course.UserCourseEnrollmentService;
import ru.misis.gamification.service.group.GroupService;
import ru.misis.gamification.service.user.UserService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceUnitTest {

    @Mock
    private UserCourseEnrollmentService enrollmentService;

    @Mock
    private CourseService courseService;

    @Mock
    private GroupService groupService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private static final String COURSE_ID = "MATH-101";
    private static final String GROUP_ID = "M-21-2";
    private static final String USER_ID = "student007";
    private static final UUID COURSE_UUID = UUID.randomUUID();
    private static final UUID GROUP_UUID = UUID.randomUUID();
    private static final UUID USER_UUID = UUID.randomUUID();

    private Course mockCourse;
    private User mockUser;
    private UserCourseEnrollment mockEnrollment;

    @BeforeEach
    void setUp() {
        mockCourse = Course.builder().uuid(COURSE_UUID).courseId(COURSE_ID).build();
        mockUser = User.builder().uuid(USER_UUID).userId(USER_ID).level(7).build();
        mockEnrollment = UserCourseEnrollment.builder()
                .user(mockUser)
                .course(mockCourse)
                .totalPointsInCourse(890)
                .build();
    }

    @Test
    void getGroupLeaderboard_withGroup_returnsCorrectDto() {
        LeaderboardEntryDto entry = LeaderboardEntryDto.builder()
                .userUuid(UUID.randomUUID())
                .userId("alice")
                .pointsInCourse(1200)
                .globalLevel(8)
                .rank(1L)
                .build();

        Page<LeaderboardEntryDto> mockPage = new PageImpl<>(
                List.of(entry),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "totalPointsInCourse")),
                25
        );

        when(courseService.getCourseUuidByExternalId(COURSE_ID)).thenReturn(COURSE_UUID);
        when(groupService.getGroupUuidByExternalIdAndCourseId(GROUP_ID, COURSE_ID)).thenReturn(GROUP_UUID);
        when(enrollmentService.findLeaderboardByCourseAndGroup(eq(COURSE_UUID), eq(GROUP_UUID), any(Pageable.class)))
                .thenReturn(mockPage);

        GroupLeaderboardPageDto result = analyticsService.getGroupLeaderboard(COURSE_ID, GROUP_ID, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getPageNumber()).isZero();
        assertThat(result.getPageSize()).isEqualTo(20);
        assertThat(result.getContent().getFirst().getUserId()).isEqualTo("alice");

        verify(courseService).getCourseUuidByExternalId(COURSE_ID);
        verify(groupService).getGroupUuidByExternalIdAndCourseId(GROUP_ID, COURSE_ID);
        verify(enrollmentService).findLeaderboardByCourseAndGroup(
                eq(COURSE_UUID), eq(GROUP_UUID), pageableCaptor.capture());

        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getSort().isSorted()).isTrue();
        assertThat(captured.getSort().toString()).contains("totalPointsInCourse: DESC");
    }

    @Test
    void getGroupLeaderboard_withoutGroup_returnsWholeCourseDto() {
        Page<LeaderboardEntryDto> mockPage = new PageImpl<>(List.of(), PageRequest.of(1, 10), 0);

        when(courseService.getCourseUuidByExternalId(COURSE_ID)).thenReturn(COURSE_UUID);
        when(enrollmentService.findLeaderboardByCourseAndGroup(eq(COURSE_UUID), eq(null), any(Pageable.class)))
                .thenReturn(mockPage);

        GroupLeaderboardPageDto result = analyticsService.getGroupLeaderboard(COURSE_ID, null, 1, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(groupService, never()).getGroupUuidByExternalIdAndCourseId(any(), any());
        verify(enrollmentService).findLeaderboardByCourseAndGroup(eq(COURSE_UUID), eq(null), any(Pageable.class));
    }

    @Test
    void getGroupLeaderboard_courseNotFound_throwsException() {
        when(courseService.getCourseUuidByExternalId(COURSE_ID))
                .thenThrow(new CourseNotFoundException(COURSE_ID));

        assertThatThrownBy(() -> analyticsService.getGroupLeaderboard(COURSE_ID, GROUP_ID, 0, 10))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessageContaining(COURSE_ID);

        verifyNoMoreInteractions(groupService, enrollmentService);
    }

    @Test
    void getGroupLeaderboard_groupNotFound_throwsException() {
        when(courseService.getCourseUuidByExternalId(COURSE_ID)).thenReturn(COURSE_UUID);
        when(groupService.getGroupUuidByExternalIdAndCourseId(GROUP_ID, COURSE_ID))
                .thenThrow(new GroupNotFoundException(GROUP_ID, COURSE_ID));

        assertThatThrownBy(() -> analyticsService.getGroupLeaderboard(COURSE_ID, GROUP_ID, 0, 10))
                .isInstanceOf(GroupNotFoundException.class);

        verifyNoMoreInteractions(enrollmentService);
    }

    @Test
    void getGroupLeaderboard_emptyResult_returnsEmptyDto() {
        when(courseService.getCourseUuidByExternalId(COURSE_ID)).thenReturn(COURSE_UUID);
        when(groupService.getGroupUuidByExternalIdAndCourseId(GROUP_ID, COURSE_ID)).thenReturn(GROUP_UUID);

        Page<LeaderboardEntryDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(enrollmentService.findLeaderboardByCourseAndGroup(any(UUID.class), any(UUID.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        GroupLeaderboardPageDto result = analyticsService.getGroupLeaderboard(COURSE_ID, GROUP_ID, 0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.isHasPrevious()).isFalse();
    }

    @Test
    void getCourseLeaderboardForUser_enrolled_returnsFullDto() {
        Page<LeaderboardEntryDto> topPage = new PageImpl<>(
                List.of(LeaderboardEntryDto.builder().userId("alice").pointsInCourse(1500).rank(1L).build()),
                PageRequest.of(0, 10),
                20
        );

        when(courseService.getCourseUuidByExternalId(COURSE_ID)).thenReturn(COURSE_UUID);
        when(userService.getUserUuidByExternalId(USER_ID)).thenReturn(USER_UUID);
        when(userService.getUserByExternalId(USER_ID)).thenReturn(mockUser);
        when(courseService.findByCourseId(COURSE_ID)).thenReturn(mockCourse);

        when(enrollmentService.isUserEnrolledInCourse(mockUser, mockCourse)).thenReturn(true);
        when(enrollmentService.findByUserAndCourse(mockUser, mockCourse)).thenReturn(mockEnrollment);
        when(enrollmentService.getRankByPointsInCourse(COURSE_UUID, null, USER_UUID)).thenReturn(3L);

        when(enrollmentService.findLeaderboardByCourseAndGroup(eq(COURSE_UUID), eq(null), any(Pageable.class)))
                .thenReturn(topPage);

        UserCourseGroupLeaderboard result = analyticsService.getCourseLeaderboardForUser(
                COURSE_ID, null, 0, 10, USER_ID);

        assertThat(result.getTopEntries()).hasSize(1);
        assertThat(result.getCurrentUserEntry()).isNotNull();
        assertThat(result.getCurrentUserEntry().getRank()).isEqualTo(3L);
        assertThat(result.getCurrentUserEntry().getPointsInCourse()).isEqualTo(890);
        assertThat(result.getCurrentUserEntry().getIsCurrentUser()).isTrue();
        assertThat(result.getCurrentUserRank()).isEqualTo(3L);
        assertThat(result.getCurrentUserPoints()).isEqualTo(890);
        assertThat(result.getPageNumber()).isZero();
        assertThat(result.getTotalElements()).isEqualTo(20);
    }

    @Test
    void getCourseLeaderboardForUser_notEnrolled_returnsDtoWithoutUserData() {
        Page<LeaderboardEntryDto> topPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(courseService.getCourseUuidByExternalId(COURSE_ID)).thenReturn(COURSE_UUID);
        when(userService.getUserUuidByExternalId(USER_ID)).thenReturn(USER_UUID);
        when(userService.getUserByExternalId(USER_ID)).thenReturn(mockUser);
        when(courseService.findByCourseId(COURSE_ID)).thenReturn(mockCourse);
        when(enrollmentService.isUserEnrolledInCourse(mockUser, mockCourse)).thenReturn(false);
        when(enrollmentService.findLeaderboardByCourseAndGroup(eq(COURSE_UUID), eq(null), any(Pageable.class)))
                .thenReturn(topPage);

        UserCourseGroupLeaderboard result = analyticsService.getCourseLeaderboardForUser(
                COURSE_ID, null, 0, 10, USER_ID);

        assertThat(result.getCurrentUserEntry()).isNull();
        assertThat(result.getCurrentUserRank()).isNull();
        assertThat(result.getCurrentUserPoints()).isNull();
        assertThat(result.getTopEntries()).isEmpty();
    }

    @Test
    void getCourseLeaderboardForUser_courseNotFound_throwsException() {
        when(courseService.getCourseUuidByExternalId(COURSE_ID))
                .thenThrow(new CourseNotFoundException(COURSE_ID));

        assertThatThrownBy(() -> analyticsService.getCourseLeaderboardForUser(
                COURSE_ID, null, 0, 10, USER_ID))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessageContaining(COURSE_ID);

        verifyNoMoreInteractions(enrollmentService, userService);
    }

    @Test
    void getCourseLeaderboardForUser_withGroup_usesGroupInQueries() {
        Page<LeaderboardEntryDto> topPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(courseService.getCourseUuidByExternalId(COURSE_ID)).thenReturn(COURSE_UUID);
        when(groupService.getGroupUuidByExternalIdAndCourseId(GROUP_ID, COURSE_ID)).thenReturn(GROUP_UUID);
        when(userService.getUserUuidByExternalId(USER_ID)).thenReturn(USER_UUID);
        when(userService.getUserByExternalId(USER_ID)).thenReturn(mockUser);
        when(courseService.findByCourseId(COURSE_ID)).thenReturn(mockCourse);
        when(enrollmentService.isUserEnrolledInCourse(mockUser, mockCourse)).thenReturn(true);
        when(enrollmentService.findByUserAndCourse(mockUser, mockCourse)).thenReturn(mockEnrollment);
        when(enrollmentService.getRankByPointsInCourse(eq(COURSE_UUID), eq(GROUP_UUID), eq(USER_UUID))).thenReturn(5L);
        when(enrollmentService.findLeaderboardByCourseAndGroup(eq(COURSE_UUID), eq(GROUP_UUID), any(Pageable.class)))
                .thenReturn(topPage);

        UserCourseGroupLeaderboard result = analyticsService.getCourseLeaderboardForUser(
                COURSE_ID, GROUP_ID, 0, 10, USER_ID);

        assertThat(result.getCurrentUserRank()).isEqualTo(5L);

        verify(enrollmentService).findLeaderboardByCourseAndGroup(eq(COURSE_UUID), eq(GROUP_UUID), any(Pageable.class));
        verify(enrollmentService).getRankByPointsInCourse(eq(COURSE_UUID), eq(GROUP_UUID), eq(USER_UUID));
    }
}