package ru.misis.gamification.service.simple.enrollment;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.UserCourseEnrollmentNotFoundException;
import ru.misis.gamification.model.LeaderboardEntryView;
import ru.misis.gamification.repository.UserCourseEnrollmentRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class EnrollmentServiceImpl implements EnrollmentService {

    /**
     * Репозиторий связей пользователь — курс
     */
    private final UserCourseEnrollmentRepository repository;

    @Override
    public boolean isUserEnrolledInCourse(@NotNull(message = "{user.required}") User user,
                                          @NotNull(message = "{course.required}") Course course) {
        return repository.existsByUserAndCourse(user, course);
    }

    @Override
    public UserCourseEnrollment findByUserAndCourse(@NotNull(message = "{user.required}") User user,
                                                    @NotNull(message = "{course.required}") Course course) {
        return repository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new UserCourseEnrollmentNotFoundException(user.getUserId(), course.getCourseId()));
    }

    @Override
    public Page<LeaderboardEntryView> findLeaderboardByCourseAndGroup(UUID courseUuid, UUID groupUuid, Pageable pageable) {
        return repository.findLeaderboardByCourseAndGroup(courseUuid, groupUuid, pageable);
    }

    @Override
    public Long getRankByPointsInCourse(UUID courseUuid, UUID groupUuid, UUID userUuid) {
        return repository.findRankByPointsInCourse(courseUuid, groupUuid, userUuid);
    }

    @Override
    public UserCourseEnrollment save(UserCourseEnrollment enrollment) {
        return repository.save(enrollment);
    }
}
