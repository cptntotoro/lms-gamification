package ru.misis.gamification.service.analytics;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.dto.analytics.GroupLeaderboardPageDto;
import ru.misis.gamification.dto.analytics.LeaderboardEntryDto;
import ru.misis.gamification.dto.analytics.UserCourseGroupLeaderboard;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.CourseNotFoundException;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.service.course.CourseService;
import ru.misis.gamification.service.course.UserCourseEnrollmentService;
import ru.misis.gamification.service.group.GroupService;
import ru.misis.gamification.service.user.UserService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Validated
public class AnalyticsServiceImpl implements AnalyticsService {

    /**
     * Сервис зачислений на курс (связь пользователь — курс)
     */
    private final UserCourseEnrollmentService enrollmentService;

    /**
     * Сервис управления курсами
     */
    private final CourseService courseService;

    /**
     * Сервис управления группами/потоками
     */
    private final GroupService groupService;

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    @Override
    public GroupLeaderboardPageDto getGroupLeaderboard(
            @NotBlank(message = "{course.id.required}") String courseId,
            @Nullable String groupId,
            @Min(value = 0, message = "{page.non-negative}") int page,
            @Min(value = 1, message = "{size.positive}") @Max(value = 100, message = "{size.too-large}") int size
    ) {
        log.debug("Запрос лидерборда группы: courseId={}, groupId={}, page={}, size={}", courseId, groupId, page, size);

        UUID courseUuid = courseService.getCourseUuidByExternalId(courseId);

        UUID groupUuid = null;
        if (groupId != null) {
            groupUuid = groupService.getGroupUuidByExternalIdAndCourseId(groupId, courseId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "totalPointsInCourse"));

        Page<LeaderboardEntryDto> pageResult = enrollmentService.findLeaderboardByCourseAndGroup(
                courseUuid, groupUuid, pageable);

        return GroupLeaderboardPageDto.builder()
                .content(pageResult.getContent())
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }

    @Override
    public UserCourseGroupLeaderboard getCourseLeaderboardForUser(
            @NotBlank(message = "{course.id.required}") String courseId,
            @Nullable String groupId,
            @Min(value = 0, message = "{page.non-negative}") int page,
            @Min(value = 1, message = "{size.positive}") @Max(value = 100, message = "{size.too-large}") int size,
            @NotBlank(message = "{user.id.required}") String currentUserId
    ) {
        log.debug("Студенческий лидерборд курса: courseId={}, groupId={}, page={}, size={}, userId={}",
                courseId, groupId, page, size, currentUserId);

        UUID courseUuid = courseService.getCourseUuidByExternalId(courseId);

        UUID groupUuid = null;
        if (StringUtils.hasText(groupId)) {
            groupUuid = groupService.getGroupUuidByExternalIdAndCourseId(groupId, courseId);
        }

        UUID currentUserUuid = userService.getUserUuidByExternalId(currentUserId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "totalPointsInCourse"));

        // Основной лидерборд (топ-N)
        Page<LeaderboardEntryDto> topPage = enrollmentService.findLeaderboardByCourseAndGroup(
                courseUuid, groupUuid, pageable);

        // Данные текущего пользователя
        LeaderboardEntryDto currentUserEntry = null;
        Long currentUserRank = null;
        Integer currentUserPoints = null;
        boolean isCurrentEnrolled;

        try {
            User currentUser = userService.getUserByExternalId(currentUserId);
            Course course = courseService.findByCourseId(courseId);

            isCurrentEnrolled = enrollmentService.isUserEnrolledInCourse(currentUser, course);

            if (isCurrentEnrolled) {
                UserCourseEnrollment enrollment = enrollmentService.findByUserAndCourse(currentUser, course);
                currentUserPoints = enrollment.getTotalPointsInCourse();

                currentUserRank = enrollmentService.getRankByPointsInCourse(
                        courseUuid,
                        groupUuid,
                        currentUserUuid
                );

                currentUserEntry = LeaderboardEntryDto.builder()
                        .userUuid(currentUserUuid)
                        .userId(currentUserId)
                        .pointsInCourse(currentUserPoints)
                        .globalLevel(currentUser.getLevel())
                        .rank(currentUserRank)
                        .isCurrentUser(true)
                        .build();
            }
        } catch (UserNotFoundException | CourseNotFoundException e) {
            log.debug("Пользователь или курс не найден: userId={}, courseId={}", currentUserId, courseId);
        }

        return UserCourseGroupLeaderboard.builder()
                .topEntries(topPage.getContent())
                .currentUserEntry(currentUserEntry)
                .currentUserRank(currentUserRank)
                .currentUserPoints(currentUserPoints)
                .pageNumber(topPage.getNumber())
                .pageSize(topPage.getSize())
                .totalElements(topPage.getTotalElements())
                .totalPages(topPage.getTotalPages())
                .hasNext(topPage.hasNext())
                .hasPrevious(topPage.hasPrevious())
                .build();
    }
}