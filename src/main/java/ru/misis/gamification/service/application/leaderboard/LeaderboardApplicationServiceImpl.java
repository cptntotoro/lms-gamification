package ru.misis.gamification.service.application.leaderboard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.CourseNotFoundException;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.model.LeaderboardEntryView;
import ru.misis.gamification.model.LeaderboardPageView;
import ru.misis.gamification.model.UserCourseGroupLeaderboardView;
import ru.misis.gamification.service.simple.course.CourseService;
import ru.misis.gamification.service.simple.enrollment.EnrollmentService;
import ru.misis.gamification.service.simple.group.GroupService;
import ru.misis.gamification.service.simple.user.UserService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Validated
public class LeaderboardApplicationServiceImpl implements LeaderboardApplicationService {

    /**
     * Сервис управления курсами (дисциплинами)
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

    /**
     * Сервис зачисления пользователей на курс и в группу (опционально)
     */
    private final EnrollmentService enrollmentService;

    @Override
    public LeaderboardPageView getGroupLeaderboard(String courseId, @Nullable String groupId, int page, int size) {
        UUID courseUuid = courseService.getCourseUuidByExternalId(courseId);
        UUID groupUuid = groupId != null ? groupService.getGroupUuidByExternalIdAndCourseId(groupId, courseId) : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "totalPointsInCourse"));
        Page<LeaderboardEntryView> pageResult = enrollmentService.findLeaderboardByCourseAndGroup(courseUuid, groupUuid, pageable);

        List<LeaderboardEntryView> content = pageResult.getContent();

        return new LeaderboardPageView(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.hasNext(),
                pageResult.hasPrevious()
        );
    }

    @Override
    public UserCourseGroupLeaderboardView getCourseLeaderboardForUser(String courseId, String groupId, int page, int size, String currentUserId) {
        log.debug("Студенческий лидерборд курса: courseId={}, groupId={}, page={}, size={}, userId={}",
                courseId, groupId, page, size, currentUserId);

        UUID courseUuid = courseService.getCourseUuidByExternalId(courseId);
        UUID groupUuid = groupId != null ? groupService.getGroupUuidByExternalIdAndCourseId(groupId, courseId) : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "totalPointsInCourse"));
        Page<LeaderboardEntryView> topPage = enrollmentService.findLeaderboardByCourseAndGroup(courseUuid, groupUuid, pageable);

        LeaderboardEntryView currentUserEntry = null;
        Long currentUserRank = null;
        Integer currentUserPoints = null;

        try {
            User user = userService.getUserByExternalId(currentUserId);
            Course course = courseService.findByCourseId(courseId);

            if (enrollmentService.isUserEnrolledInCourse(user, course)) {
                UserCourseEnrollment enrollment = enrollmentService.findByUserAndCourse(user, course);
                currentUserPoints = enrollment.getTotalPointsInCourse();
                currentUserRank = enrollmentService.getRankByPointsInCourse(courseUuid, groupUuid, user.getUuid());

                currentUserEntry = new LeaderboardEntryView(
                        user.getUuid(),
                        currentUserId,
                        currentUserPoints,
                        user.getLevel(),
                        currentUserRank,
                        true
                );
            }
        } catch (CourseNotFoundException | UserNotFoundException e) {
            log.debug("Пользователь или курс не найден: userId={}, courseId={}", currentUserId, courseId);
        }

        List<LeaderboardEntryView> topEntries = topPage.getContent();

        return new UserCourseGroupLeaderboardView(
                topEntries,
                currentUserEntry,
                currentUserRank,
                currentUserPoints,
                topPage.getNumber(),
                topPage.getSize(),
                topPage.getTotalElements(),
                topPage.getTotalPages(),
                topPage.hasNext(),
                topPage.hasPrevious()
        );
    }
}
