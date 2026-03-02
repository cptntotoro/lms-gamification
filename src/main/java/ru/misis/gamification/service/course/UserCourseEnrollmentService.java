package ru.misis.gamification.service.course;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.dto.analytics.LeaderboardEntryDto;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.UserCourseEnrollmentNotFoundException;

import java.util.UUID;

/**
 * Сервис зачислений на курс (связь пользователь — курс)
 */
public interface UserCourseEnrollmentService {

    /**
     * Проверить, зачислен ли пользователь на курс
     *
     * @param user   Пользователь
     * @param course Курс (дисциплина)
     * @return Да / Нет
     * @throws ConstraintViolationException если user == null или course == null
     */
    boolean isUserEnrolledInCourse(@NotNull(message = "{user.required}") User user,
                                   @NotNull(message = "{course.required}") Course course);

    /**
     * Получить зачисление пользователя на курс
     *
     * @param user   Пользователь
     * @param course Курс (дисциплина)
     * @return Зачисление на курс (связь пользователь — курс) {@link UserCourseEnrollment}
     * @throws UserCourseEnrollmentNotFoundException если пользователь не зачислен на курс
     * @throws ConstraintViolationException          если user == null или course == null
     */
    UserCourseEnrollment findByUserAndCourse(@NotNull(message = "{user.required}") User user,
                                             @NotNull(message = "{course.required}") Course course);

    /**
     * Сохранить или обновить зачисление пользователя на курс
     *
     * @param enrollment Зачисление на курс (связь пользователь — курс) {@link UserCourseEnrollment}
     * @return Зачисление на курс (связь пользователь — курс) {@link UserCourseEnrollment}
     * @throws ConstraintViolationException если enrollment == null
     */
    UserCourseEnrollment save(@NotNull(message = "{enrollment.required}") UserCourseEnrollment enrollment);

    long countByCourseUuidAndTotalPointsInCourseGreaterThan(
            @NotNull UUID courseUuid,
            @NotNull Integer totalPointsInCourse
    );

    Integer findTotalPointsInCourseByUserAndCourse(
            @NotNull User user,
            @NotNull Course course
    );

    Page<LeaderboardEntryDto> findLeaderboardByCourseAndGroup(UUID courseUuid, UUID groupUuid, Pageable pageable);
}
