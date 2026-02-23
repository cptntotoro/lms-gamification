package ru.misis.gamification.service.user;

import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.model.entity.User;

/**
 * Сервис управления пользователями
 */
public interface UserService {

    /**
     * Получить пользователя по идентификатору из LMS
     * Если пользователь не найден — бросает исключение
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Пользователь
     * @throws UserNotFoundException если пользователь не существует
     */
    User get(String userId) throws UserNotFoundException;

    /**
     * Создать пользователя, если он не существует, иначе возвращает существующего.
     * Используется при первом взаимодействии с пользователем.
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Пользователь (существующий или только что созданный)
     */
    User createIfNotExists(String userId);

    @Transactional
    User getOrCreateLocked(String userId);

    /**
     * Обновить пользователя
     *
     * @param user Пользователь
     * @return Пользователь
     * @throws IllegalArgumentException если у пользователя нет UUID
     */
    User update(User user);
}
