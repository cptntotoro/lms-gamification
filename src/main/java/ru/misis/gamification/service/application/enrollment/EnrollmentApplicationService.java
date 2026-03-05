package ru.misis.gamification.service.application.enrollment;

import ru.misis.gamification.model.CourseEnrollmentSummary;

import java.util.UUID;

public interface EnrollmentApplicationService {

//    /**
//     * Зачислить пользователя на курс (и группу, если указана)
//     * Если курсы отключены — ничего не делает
//     *
//     * @param user     Пользователь
//     * @param courseId Идентификатор курса из LMS
//     * @param groupId  Идентификатор группы из LMS
//     * @throws ConstraintViolationException если user == null
//     */
//    void enrollIfNeeded(@NotNull(message = "{user.required}") User user, String courseId, String groupId);
//
//    /**
//     * Начислить очки пользователю по конкретному курсу
//     *
//     * @param user       Пользователь
//     * @param courseUuid UUID курса
//     * @param points     Количество очков
//     * @throws ConstraintViolationException если user == null или courseUuid == null
//     */
//    void addPointsToCourse(@NotNull(message = "{user.required}") User user,
//                           @NotNull(message = "{course.uuid.required}") UUID courseUuid,
//                           int points);

    /**
     * Зачислить пользователя на курс (и группу, если указана).
     * Если курсы отключены — ничего не делает
     *
     * @param userId   Идентификатор пользователя из LMS
     * @param courseId Идентификатор курса из LMS
     * @param groupId  Идентификатор группы из LMS
     */
    void enrollIfNeeded(String userId, String courseId, String groupId);

    /**
     * Начислить очки пользователю по конкретному курсу
     *
     * @param userId     Идентификатор пользователя из LMS
     * @param courseUuid UUID курса
     * @param points     Количество очков
     */
    void addPointsToCourse(String userId, UUID courseUuid, int points);

    /**
     * Получить модель зачисления на курс
     *
     * @param userId   Идентификатор пользователя из LMS
     * @param courseId Идентификатор курса из LMS
     * @return Модель зачисления на курс
     */
    CourseEnrollmentSummary getEnrollmentSummary(String userId, String courseId);
}
