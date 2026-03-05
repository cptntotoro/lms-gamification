package ru.misis.gamification.service.application.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
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

    private final UserService userSimpleService;
    private final CourseService courseSimpleService;
    private final GroupService groupSimpleService;
    private final EnrollmentService enrollmentSimpleService;
    private final UserProgressApplicationService progressApplicationService;

    @Override
    public UserStatisticsView getUserStatistics(String courseId, String groupId, String userId) {
        User user = userSimpleService.getUserByExternalId(userId);
        Course course = courseSimpleService.findByCourseId(courseId);

        UUID courseUuid = course.getUuid();
        UUID groupUuid = groupId != null ? groupSimpleService.getGroupUuidByExternalIdAndCourseId(groupId, courseId) : null;

        if (!enrollmentSimpleService.isUserEnrolledInCourse(user, course)) {
            throw new RuntimeException("Пользователь не зачислен на курс"); // или ваше исключение
        }

        UserCourseEnrollment enrollment = enrollmentSimpleService.findByUserAndCourse(user, course);

        Long rankInCourse = enrollmentSimpleService.getRankByPointsInCourse(courseUuid, null, user.getUuid());
        Long rankInGroup = groupUuid != null ?
                enrollmentSimpleService.getRankByPointsInCourse(courseUuid, groupUuid, user.getUuid()) : null;

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
