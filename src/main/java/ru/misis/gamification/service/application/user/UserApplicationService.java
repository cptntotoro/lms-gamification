package ru.misis.gamification.service.application.user;

import ru.misis.gamification.model.UserSummary;

/**
 * Фасадный сервис управления пользователями
 */
public interface UserApplicationService {

    /**
     * Создать пользователя с привязкой к курсу и группе
     *
     * @param userId   Идентификатор пользователя из LMS
     * @param courseId Идентификатор курса из LMS
     * @param groupId  Идентификатор группы из LMS
     * @return Модель пользователя
     */
    UserSummary createIfNotExists(String userId, String courseId, String groupId);

    /**
     * Получить модель пользователя по идентификатору из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Модель пользователя
     */
    UserSummary getUserSummary(String userId);
}
