package ru.misis.gamification.service.application.enrollment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.Group;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.model.CourseEnrollmentSummary;
import ru.misis.gamification.service.simple.course.CourseService;
import ru.misis.gamification.service.simple.enrollment.EnrollmentService;
import ru.misis.gamification.service.simple.group.GroupService;
import ru.misis.gamification.service.simple.user.UserService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@Validated
public class EnrollmentApplicationServiceImpl implements EnrollmentApplicationService {

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

    /**
     * Сервис зачисления пользователей на курс и в группу (опционально)
     */
    private final EnrollmentService enrollmentService;

    @Value("${gamification.features.courses.enabled:true}")
    private boolean coursesEnabled;

    @Override
    public void enrollIfNeeded(String userId, String courseId, String groupId) {
        if (!coursesEnabled || courseId == null || courseId.trim().isEmpty()) {
            return;
        }

        User user = userService.getUserByExternalId(userId);
        Course course = courseService.findByCourseId(courseId);

        if (enrollmentService.isUserEnrolledInCourse(user, course)) {
            log.debug("Пользователь {} уже зачислен на курс {}", userId, courseId);
            return;
        }

        Group group = null;
        if (groupId != null && !groupId.trim().isEmpty()) {
            UUID groupUuid = groupService.getGroupUuidByExternalIdAndCourseId(groupId, courseId);
            group = groupService.findById(groupUuid);
        }

        UserCourseEnrollment enrollment = UserCourseEnrollment.builder()
                .user(user)
                .course(course)
                .group(group)
                .totalPointsInCourse(0)
                .build();

        enrollmentService.save(enrollment);

        log.info("Пользователь {} зачислен на курс {} (группа: {})",
                userId, courseId, group != null ? group.getGroupId() : "без группы");
    }

    @Override
    public void addPointsToCourse(String userId, UUID courseUuid, int points) {
        if (!coursesEnabled || points <= 0) {
            return;
        }

        User user = userService.getUserByExternalId(userId);
        Course course = courseService.findById(courseUuid);

        UserCourseEnrollment enrollment = enrollmentService.findByUserAndCourse(user, course);

        enrollment.setTotalPointsInCourse(enrollment.getTotalPointsInCourse() + points);
        enrollmentService.save(enrollment);

        log.debug("Начислено {} очков по курсу {} (UUID: {}) пользователю {}",
                points, course.getCourseId(), courseUuid, userId);
    }

    @Override
    public CourseEnrollmentSummary getEnrollmentSummary(String userId, String courseId) {
        User user = userService.getUserByExternalId(userId);
        Course course = courseService.findByCourseId(courseId);

        if (!enrollmentService.isUserEnrolledInCourse(user, course)) {
            return null; // TODO: или бросить исключение — по вашему выбору
        }

        UserCourseEnrollment enrollment = enrollmentService.findByUserAndCourse(user, course);

        return new CourseEnrollmentSummary(
                user.getUuid(),
                userId,
                course.getUuid(),
                courseId,
                enrollment.getGroup() != null ? enrollment.getGroup().getUuid() : null,
                enrollment.getGroup() != null ? enrollment.getGroup().getGroupId() : null,
                enrollment.getTotalPointsInCourse()
        );
    }
}
