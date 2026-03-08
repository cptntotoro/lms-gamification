package ru.misis.gamification.service.simple.enrollment;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.UserCourseEnrollmentNotFoundException;
import ru.misis.gamification.model.LeaderboardEntryView;

import java.util.List;
import java.util.UUID;

/**
 * Сервис зачисления пользователей на курс и в группу (опционально)
 */
public interface EnrollmentService {

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
     * Получить страницу лидерборда студентов на курсе
     *
     * @param courseUuid UUID курса
     * @param groupUuid  UUID группы
     * @param pageable   Параметры пагинации и сортировки
     * @return Страница элементов лидерборда группы по курсу
     */
    Page<LeaderboardEntryView> findLeaderboardByCourseAndGroup(UUID courseUuid, UUID groupUuid, Pageable pageable);

    /**
     * Получить текущее место (ранг) пользователя в лидерборде курса или группы
     * <p>
     * Ранг рассчитывается с помощью {@code DENSE_RANK()}:
     * <ul>
     *     <li>студенты с одинаковым количеством очков получают одинаковый ранг</li>
     *     <li>следующий ранг не пропускается (dense rank)</li>
     * </ul>
     * </p>
     *
     * @param courseUuid      UUID курса
     * @param groupUuid       UUID группы (может быть {@code null} для общего лидерборда курса)
     * @param currentUserUuid UUID пользователя
     * @return ранг пользователя (1 = лидер)
     */
    Long getRankByPointsInCourse(UUID courseUuid, UUID groupUuid, UUID currentUserUuid);

    /**
     * Сохранить или обновить зачисление пользователя на курс
     *
     * @param enrollment Зачисление на курс (связь пользователь — курс) {@link UserCourseEnrollment}
     * @return Зачисление на курс (связь пользователь — курс) {@link UserCourseEnrollment}
     * @throws ConstraintViolationException если enrollment == null
     */
    UserCourseEnrollment save(@NotNull(message = "{enrollment.required}") UserCourseEnrollment enrollment);

    /**
     * Получить все зачисления пользователя на курсы
     *
     * @param user Пользователь
     * @return Список зачислений на курсы
     */
    List<UserCourseEnrollment> findAllByUser(User user);
}
