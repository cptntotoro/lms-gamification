package ru.misis.gamification.service.course;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.dto.analytics.LeaderboardEntryDto;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.UserCourseEnrollmentNotFoundException;
import ru.misis.gamification.repository.UserCourseEnrollmentRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
public class UserCourseEnrollmentServiceImpl implements UserCourseEnrollmentService {

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
    public UserCourseEnrollment save(@NotNull(message = "{enrollment.required}") UserCourseEnrollment enrollment) {
        return repository.save(enrollment);
    }

    @Override
    public long countByCourseUuidAndTotalPointsInCourseGreaterThan(@NotNull(message = "{course.uuid.required}") UUID courseUuid,
                                                                   @NotNull(message = "{points.required}") Integer totalPointsInCourse) {
        return repository.countByCourseUuidAndTotalPointsInCourseGreaterThan(courseUuid, totalPointsInCourse);
    }

    @Override
    public Integer findTotalPointsInCourseByUserAndCourse(@NotNull(message = "{user.required}") User user,
                                                          @NotNull(message = "{course.required}") Course course) {
        return repository.findTotalPointsInCourseByUserUuidAndCourseUuid(user.getUuid(), course.getUuid())
                .orElse(0);
    }

    @Override
    public Page<LeaderboardEntryDto> findLeaderboardByCourseAndGroup(UUID courseUuid, UUID groupUuid, Pageable pageable) {
        return repository.findLeaderboardByCourseAndGroup(courseUuid, groupUuid, pageable);
    }
}
