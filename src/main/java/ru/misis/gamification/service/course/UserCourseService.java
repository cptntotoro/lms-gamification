package ru.misis.gamification.service.course;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import ru.misis.gamification.entity.User;

import java.util.UUID;

/**
 * Сервис управления курсами пользователей
 */
public interface UserCourseService {

    /**
     * Зачислить пользователя на курс (и группу, если указана)
     * Если курсы отключены — ничего не делает
     *
     * @param user     Пользователь
     * @param courseId Идентификатор курса из LMS
     * @param groupId  Идентификатор группы из LMS
     * @throws ConstraintViolationException если user == null
     */
    void enrollIfNeeded(@NotNull(message = "{user.required}") User user, String courseId, String groupId);

    /**
     * Начислить очки пользователю по конкретному курсу
     *
     * @param user       Пользователь
     * @param courseUuid UUID курса
     * @param points     Количество очков
     * @throws ConstraintViolationException если user == null или courseUuid == null
     */
    void addPointsToCourse(@NotNull(message = "{user.required}") User user,
                           @NotNull(message = "{course.uuid.required}") UUID courseUuid,
                           int points);
}
