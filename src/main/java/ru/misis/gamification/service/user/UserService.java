package ru.misis.gamification.service.user;

import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.model.entity.User;

/**
 * Сервис управления пользователями
 */
public interface UserService {

    /**
     * Создать пользователя, если его не существует
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Пользователь
     */
    User createIfNotExists(String userId);

    /**
     * Получить пользователя
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    User get(String userId);

    /**
     * Обновить пользователя
     *
     * @param user Пользователь
     * @return Пользователь
     */
    User update(User user);
}
