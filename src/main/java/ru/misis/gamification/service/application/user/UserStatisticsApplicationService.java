package ru.misis.gamification.service.application.user;

import ru.misis.gamification.model.UserStatisticsView;

/**
 * Фасадный сервис управления статистикой пользователей
 */
public interface UserStatisticsApplicationService {

    /**
     * Получить статистику пользователя по группе и курсу
     *
     * @param userId   Идентификатор пользователя из LMS
     * @param courseId Идентификатор курса из LMS
     * @param groupId  Идентификатор группы из LMS
     * @return Модель статистики пользователя
     */
    UserStatisticsView getUserStatistics(String userId, String courseId, String groupId);
}
