package ru.misis.gamification.service.simple.user;

import jakarta.validation.ConstraintViolationException;
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
    User createIfNotExists(String userId, String courseId, String groupId);

    /**
     * Получить пользователя по идентификатору из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Пользователь
     * @throws UserNotFoundException        если пользователь не существует
     * @throws ConstraintViolationException если userId == null или пустая строка
     */
    User get(String userId) throws UserNotFoundException;

    /**
     * Обновить пользователя
     *
     * @param user Пользователь
     * @return Пользователь
     * @throws IllegalArgumentException     если у пользователя нет UUID
     * @throws ConstraintViolationException если user == null
     */
    User update(User user);

    /**
     * Получить страницу всех пользователей с поддержкой пагинации и сортировки
     *
     * @param courseId Идентификатор курса из LMS
     * @param groupId  Идентификатор группы из LMS
     * @param pageable Параметры пагинации и сортировки
     * @return Страница пользователей
     * @throws ConstraintViolationException если pageable == null
     */
    Page<User> findAll(String courseId, String groupId, Pageable pageable);

    /**
     * Получить UUID пользователя по идентификатору пользователя из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return UUID пользователя
     * @throws UserNotFoundException        если пользователь не найден
     * @throws ConstraintViolationException если userId == null или пустая строка
     */
    UUID getUserUuidByExternalId(String userId);

    /**
     * Получить пользователя по идентификатору из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Пользователь
     * @throws IllegalArgumentException если userId пустой или null
     * @throws UserNotFoundException    если пользователь не найден
     */
    User getUserByExternalId(String userId) throws UserNotFoundException;

    /**
     * Получить пользователя по UUID
     *
     * @param uuid UUID пользователя
     * @return Пользователь
     * @throws UserNotFoundException                           если пользователь не найден
     * @throws ConstraintViolationException если userId == null или пустая строка
     */
    User getByUuid(UUID uuid) throws UserNotFoundException;
}
