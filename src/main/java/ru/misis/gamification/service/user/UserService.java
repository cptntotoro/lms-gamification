package ru.misis.gamification.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.model.entity.User;

/**
 * Сервис управления пользователями
 */
public interface UserService {

    /**
     * Создать пользователя, если он не существует, иначе возвращает существующего.
     * Используется при первом взаимодействии с пользователем.
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Пользователь (существующий или только что созданный)
     */
    User createIfNotExists(String userId);

    /**
     * Создать пользователя с поддержкой курса и группы, если он не существует, иначе возвращает существующего
     *
     * @param userId   Идентификатор пользователя из LMS
     * @param courseId Идентификатор курса из LMS
     * @param groupId  Идентификатор группы из LMS
     * @return Пользователь (существующий или только что созданный)
     */
    User createIfNotExists(String userId, String courseId, String groupId);

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
     * Получить пользователя по идентификатору из LMS с пессимистической блокировкой на запись
     * <p>
     * Если пользователь существует — возвращает его с наложенной блокировкой {@code PESSIMISTIC_WRITE}
     * (FOR UPDATE), что позволяет безопасно обновлять сущность в рамках текущей транзакции без риска
     * race condition.
     * </p>
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Пользователь (существующий с блокировкой или только что созданный)
     */
    User getOrCreateLocked(String userId);

    /**
     * Обновить пользователя
     *
     * @param user Пользователь
     * @return Пользователь
     * @throws IllegalArgumentException если у пользователя нет UUID
     */
    User update(User user);

    /**
     * Получить страницу всех пользователей с поддержкой пагинации и сортировки
     * <p>
     * Метод предназначен в основном для административных целей.
     * </p>
     *
     * @param pageable Параметры пагинации и сортировки
     * @return Страница пользователей
     */
    Page<User> findAll(Pageable pageable);
}
