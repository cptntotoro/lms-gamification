package ru.misis.gamification.service.application.user;

import ru.misis.gamification.model.UserStatisticsView;

/**
 * Фасадный сервис управления статистикой пользователей
 */
public interface UserStatisticsApplicationService {

    /**
     * Получить статистику пользователя по группе и курсу
     *
     * @param courseId Идентификатор курса из LMS
     * @param groupId  Идентификатор группы из LMS
     * @param userId   Идентификатор пользователя из LMS
     * @return Модель статистики пользователя
     */
    UserStatisticsView getUserStatistics(String courseId, String groupId, String userId);
}
