package ru.misis.gamification.service.user;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.exception.UserNotFoundException;

import java.util.UUID;

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
     * @throws ConstraintViolationException если userId == null или пустая строка
     */
    User createIfNotExists(@NotBlank(message = "{user.id.required}") String userId,
                           String courseId,
                           String groupId);

    /**
     * Получить пользователя по идентификатору из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Пользователь
     * @throws UserNotFoundException        если пользователь не существует
     * @throws ConstraintViolationException если userId == null или пустая строка
     */
    User get(@NotBlank(message = "{user.id.required}") String userId) throws UserNotFoundException;

    /**
     * Обновить пользователя
     *
     * @param user Пользователь
     * @return Пользователь
     * @throws IllegalArgumentException     если у пользователя нет UUID
     * @throws ConstraintViolationException если user == null
     */
    User update(@NotNull(message = "{user.required}") User user);

    /**
     * Получить страницу всех пользователей с поддержкой пагинации и сортировки
     *
     * @param pageable Параметры пагинации и сортировки
     * @return Страница пользователей
     * @throws ConstraintViolationException если pageable == null
     */
    Page<User> findAll(@NotNull(message = "{pageable.required}") Pageable pageable);

    /**
     * Получить UUID пользователя по идентификатору пользователя из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return UUID пользователя
     * @throws UserNotFoundException        если пользователь не найден
     * @throws ConstraintViolationException если userId == null или пустая строка
     */
    UUID getUserUuidByExternalId(@NotBlank(message = "{user.id.required}") String userId);

    /**
     * Получить пользователя по идентификатору из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Пользователь
     * @throws IllegalArgumentException если userId пустой или null
     * @throws UserNotFoundException    если пользователь не найден
     */
    User getUserByExternalId(@NotBlank(message = "{user.id.required}") String userId) throws UserNotFoundException;

    /**
     * Получить пользователя по UUID
     *
     * @param uuid UUID пользователя
     * @return Пользователь
     * @throws UserNotFoundException                           если пользователь не найден
     * @throws jakarta.validation.ConstraintViolationException если userId == null или пустая строка
     */
    User getByUuid(@NotNull(message = "{user.uuid.required}") UUID uuid) throws UserNotFoundException;
}
