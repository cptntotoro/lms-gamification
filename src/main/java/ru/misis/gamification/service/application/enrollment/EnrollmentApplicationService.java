package ru.misis.gamification.service.application.enrollment;

import ru.misis.gamification.model.CourseEnrollmentSummary;

import java.util.UUID;

/**
 * Фасадный сервис управления зачислениями пользователей на курсы и в группы
 */
public interface EnrollmentApplicationService {

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
