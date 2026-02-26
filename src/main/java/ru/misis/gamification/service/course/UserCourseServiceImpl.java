package ru.misis.gamification.service.course;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.exception.CourseNotFoundException;
import ru.misis.gamification.exception.GroupNotFoundException;
import ru.misis.gamification.exception.UserNotEnrolledInCourseException;
import ru.misis.gamification.model.entity.Course;
import ru.misis.gamification.model.entity.Group;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.model.entity.UserCourseEnrollment;
import ru.misis.gamification.repository.CourseRepository;
import ru.misis.gamification.repository.GroupRepository;
import ru.misis.gamification.repository.UserCourseEnrollmentRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserCourseServiceImpl implements UserCourseService {

    /**
     * Репозиторий курсов
     */
    private final CourseRepository courseRepository;

    /**
     * Репозиторий групп
     */
    private final GroupRepository groupRepository;

    /**
     * Репозиторий связей студент — курс
     */
    private final UserCourseEnrollmentRepository enrollmentRepository;

    /**
     * Флаг поддержки курсов и групп
     */
    @Value("${gamification.features.courses.enabled:true}")
    private boolean coursesEnabled;

    @Override
    public void enrollIfNeeded(User user, String courseId, String groupId) {
        if (!coursesEnabled || courseId == null) {
            return;
        }

        Course course = courseRepository.findByCourseId(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        Group group = null;
        if (groupId != null) {
            group = groupRepository.findByGroupIdAndCourse(groupId, course)
                    .orElseThrow(() -> new GroupNotFoundException(groupId, courseId));
        }

        if (enrollmentRepository.existsByUserAndCourse(user, course)) {
            return;
        }

        UserCourseEnrollment enrollment = UserCourseEnrollment.builder()
                .user(user)
                .course(course)
                .group(group)
                .totalPointsInCourse(0)
                .build();

        enrollmentRepository.save(enrollment);
        log.info("Пользователь {} зачислен на курс {} (группа: {})",
                user.getUserId(), courseId, groupId);
    }

    @Override
    public void addPointsToCourse(User user, String courseId, int points) {
        if (!coursesEnabled || courseId == null || points <= 0) {
            return;
        }

        UserCourseEnrollment enrollment = enrollmentRepository
                .findByUserAndCourseCourseId(user, courseId)
                .orElseThrow(() -> new UserNotEnrolledInCourseException(user.getUserId(), courseId));

        enrollment.setTotalPointsInCourse(enrollment.getTotalPointsInCourse() + points);
        enrollmentRepository.save(enrollment);

        log.debug("Начислено {} очков по курсу {} пользователю {}",
                points, courseId, user.getUserId());
    }
}