package ru.misis.gamification.service.application.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.UserNotEnrolledInCourseException;
import ru.misis.gamification.model.UserProgressView;
import ru.misis.gamification.model.UserStatisticsView;
import ru.misis.gamification.service.simple.course.CourseService;
import ru.misis.gamification.service.simple.enrollment.EnrollmentService;
import ru.misis.gamification.service.simple.group.GroupService;
import ru.misis.gamification.service.simple.user.UserService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Validated
public class UserStatisticsApplicationServiceImpl implements UserStatisticsApplicationService {

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    /**
     * Сервис управления курсами
     */
    private final CourseService courseService;

    /**
     * Сервис управления группами/потоками
     */
    private final GroupService groupService;

    /**
     * Сервис зачисления пользователей на курс и в группу (опционально)
     */
    private final EnrollmentService enrollmentService;

    /**
     * Фасадный сервис управления прогрессом очков и уровня пользователей
     */
    private final UserProgressApplicationService progressApplicationService;

    @Override
    public UserStatisticsView getUserStatistics(String userId, String courseId, String groupId) {
        User user = userService.getUserByExternalId(userId);
        Course course = courseService.findByCourseId(courseId);

        UUID courseUuid = course.getUuid();
        UUID groupUuid = groupId != null ? groupService.getGroupUuidByExternalIdAndCourseId(groupId, courseId) : null;

        if (!enrollmentService.isUserEnrolledInCourse(user, course)) {
            throw new UserNotEnrolledInCourseException(userId, courseId);
        }

        UserCourseEnrollment enrollment = enrollmentService.findByUserAndCourse(user, course);

        Long rankInCourse = enrollmentService.getRankByPointsInCourse(courseUuid, null, user.getUuid());
        Long rankInGroup = groupUuid != null ?
                enrollmentService.getRankByPointsInCourse(courseUuid, groupUuid, user.getUuid()) : null;

        UserProgressView progress = progressApplicationService.getProgress(userId);

        return new UserStatisticsView(
                userId,
                user.getLevel(),
                user.getTotalPoints(),
                courseId,
                groupId,
                enrollment.getTotalPointsInCourse(),
                rankInCourse,
                rankInGroup,
                progress.pointsToNextLevel(),
                progress.progressPercent()
        );
    }
}
