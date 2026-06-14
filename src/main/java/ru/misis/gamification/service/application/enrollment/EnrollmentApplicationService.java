package ru.misis.gamification.service.application.enrollment;

import ru.misis.gamification.entity.Group;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.model.CourseEnrollmentSummary;
import ru.misis.gamification.model.EnrollmentResult;

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
    EnrollmentResult enrollIfNeeded(String userId, String courseId, String groupId);

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

    /**
     * Проверить, состоит ли пользователь в указанной группе на данном курсе
     *
     * @param user  пользователь
     * @param group группа
     * @return true, если пользователь зачислен на курс и привязан к этой группе
     */
    boolean isUserInGroup(User user, Group group);
}
