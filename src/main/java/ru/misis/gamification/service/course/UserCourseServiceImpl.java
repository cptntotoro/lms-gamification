//package ru.misis.gamification.service.course;
//
//import jakarta.validation.constraints.NotNull;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.validation.annotation.Validated;
//import ru.misis.gamification.entity.Course;
//import ru.misis.gamification.entity.Group;
//import ru.misis.gamification.entity.User;
//import ru.misis.gamification.entity.UserCourseEnrollment;
//import ru.misis.gamification.service.simple.group.GroupService;
//import ru.misis.gamification.service.simple.course.CourseService;
//
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//@Slf4j
//@Validated
//public class UserCourseServiceImpl implements UserCourseService {
//
//    /**
//     * Сервис управления курсами
//     */
//    private final CourseService courseService;
//
//    /**
//     * Сервис управления группами/потоками
//     */
//    private final GroupService groupService;
//
//    /**
//     * Сервис зачислений на курс (связь пользователь — курс)
//     */
//    private final UserCourseEnrollmentService enrollmentService;
//
//    /**
//     * Флаг поддержки курсов и групп
//     */
//    @Value("${gamification.features.courses.enabled:true}")
//    private boolean coursesEnabled;
//
//    @Override
//    public void enrollIfNeeded(@NotNull(message = "{user.required}") User user, String courseId, String groupId) {
//        if (!coursesEnabled || courseId == null || courseId.trim().isEmpty()) {
//            return;
//        }
//
//        Course course = courseService.findByCourseId(courseId);
//
//        Group group = null;
//        if (groupId != null && !groupId.trim().isEmpty()) {
//            UUID groupUuid = groupService.getGroupUuidByExternalIdAndCourseId(groupId, courseId);
//            group = groupService.findById(groupUuid);
//        }
//
//        if (enrollmentService.isUserEnrolledInCourse(user, course)) {
//            log.debug("Пользователь {} уже зачислен на курс {}", user.getUserId(), courseId);
//            return;
//        }
//
//        UserCourseEnrollment enrollment = UserCourseEnrollment.builder().user(user).course(course).group(group).totalPointsInCourse(0).build();
//        enrollmentService.save(enrollment);
//
//        log.info("Пользователь {} зачислен на курс {} (группа: {})", user.getUserId(), course.getCourseId(), group != null ? group.getGroupId() : "без группы");
//    }
//
//    @Override
//    public void addPointsToCourse(@NotNull(message = "{user.required}") User user,
//                                  @NotNull(message = "{course.uuid.required}") UUID courseUuid,
//                                  int points) {
//        if (!coursesEnabled || points <= 0) {
//            return;
//        }
//
//        Course course = courseService.findById(courseUuid);
//        UserCourseEnrollment enrollment = enrollmentService.findByUserAndCourse(user, course);
//
//        enrollment.setTotalPointsInCourse(enrollment.getTotalPointsInCourse() + points);
//        enrollmentService.save(enrollment);
//
//        log.debug("Начислено {} очков по курсу {} (UUID: {}) пользователю {}", points, course.getCourseId(), courseUuid, user.getUserId());
//    }
//}