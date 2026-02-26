package ru.misis.gamification.service.course;

import ru.misis.gamification.model.entity.User;

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
     */
    void enrollIfNeeded(User user, String courseId, String groupId);

    /**
     * Начислить очки пользователю по конкретному курсу
     *
     * @param user     Пользователь
     * @param courseId Идентификатор курса из LMS
     * @param points   Количество очков
     */
    void addPointsToCourse(User user, String courseId, int points);
}
