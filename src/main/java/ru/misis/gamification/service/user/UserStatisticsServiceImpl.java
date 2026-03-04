package ru.misis.gamification.service.user;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.dto.user.response.UserStatisticsDto;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.UserNotEnrolledInCourseException;
import ru.misis.gamification.service.analytics.AnalyticsService;
import ru.misis.gamification.service.course.CourseService;
import ru.misis.gamification.service.course.UserCourseEnrollmentService;
import ru.misis.gamification.service.group.GroupService;
import ru.misis.gamification.service.progress.UserProgressService;
import ru.misis.gamification.service.progress.calculator.ProgressCalculator;
import ru.misis.gamification.service.progress.result.ProgressMetrics;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserStatisticsServiceImpl implements UserStatisticsService {

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    /**
     * Сервис управления курсами (дисциплинами) {@link Course}
     */
    private final CourseService courseService;

    /**
     * Сервис управления группами/потоками (группами пользователей внутри курса)
     */
    private final GroupService groupService;

    /**
     * Сервис зачислений на курс (связь пользователь — курс)
     */
    private final UserCourseEnrollmentService enrollmentService;
    private final UserProgressService userProgressService;   // для глобального прогресса
    private final AnalyticsService analyticsService;

    /**
     * Компонент расчёта метрик прогресса пользователя
     */
    private final ProgressCalculator progressCalculator;

    @Override
    public UserStatisticsDto getUserStatisticsGlobalAndByCourseAndGroup(String courseId, @Nullable String groupId, String userId) {
        User user = userService.getUserByExternalId(userId);
        Course course = courseService.findByCourseId(courseId); // кидает исключение если не найден

        UUID courseUuid = course.getUuid();
        UUID groupUuid = null;

        if (groupId != null) {
            groupUuid = groupService.getGroupUuidByExternalIdAndCourseId(groupId, courseId);
        }

        // Проверяем, зачислен ли пользователь на курс
        if (!enrollmentService.isUserEnrolledInCourse(user, course)) {
            throw new UserNotEnrolledInCourseException(userId, courseId);
        }

        UserCourseEnrollment enrollment = enrollmentService.findByUserAndCourse(user, course);

        // Глобальный прогресс
        ProgressMetrics globalMetrics = progressCalculator.calculate(user);

        // Ранг(и)
        Long rankInCourse = enrollmentService.getRankByPointsInCourse(courseUuid, null, user.getUuid());
        Long rankInGroup = null;
        if (groupUuid != null) {
            rankInGroup = enrollmentService.getRankByPointsInCourse(courseUuid, groupUuid, user.getUuid());
        }

        return UserStatisticsDto.builder()
                .userId(userId)
                .globalLevel(user.getLevel())
                .totalPoints(user.getTotalPoints())
                .courseId(courseId)
                .groupId(groupId)
                .pointsInCourse(enrollment.getTotalPointsInCourse())
                .rankInCourse(rankInCourse)
                .rankInGroup(rankInGroup)
                .pointsToNextGlobalLevel(globalMetrics.getPointsToNextLevel())
                .progressPercent(globalMetrics.getProgressPercent())
                .build();
    }
}
