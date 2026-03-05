package ru.misis.gamification.service.simple.group;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.misis.gamification.entity.Group;
import ru.misis.gamification.exception.GroupNotFoundException;

import java.util.UUID;

/**
 * Сервис управления группами/потоками (группами пользователей внутри курса)
 */
public interface GroupService {

    /**
     * Проверить существование группы по её внешнему идентификатору из LMS внутри указанного курса
     *
     * @param groupId  Идентификатор группы из LMS
     * @param courseId Идентификатор курса из LMS
     * @return Да / Нет
     * @throws ConstraintViolationException если groupId или courseId — null или пустая строка
     */
    boolean existsByGroupIdAndCourseId(
            @NotBlank(message = "{group.id.required}") String groupId,
            @NotBlank(message = "{course.id.required}") String courseId);

    /**
     * Получить UUID группы по идентификатору из LMS и идентификатору курса из LMS
     *
     * @param groupId  Идентификатор группы из LMS
     * @param courseId Идентификатор курса из LMS
     * @return UUID группы
     * @throws GroupNotFoundException       если группа с указанными идентификаторами не найдена
     * @throws ConstraintViolationException если groupId или courseId — null или пустая строка
     */
    UUID getGroupUuidByExternalIdAndCourseId(
            @NotBlank(message = "{group.id.required}") String groupId,
            @NotBlank(message = "{course.id.required}") String courseId);

    /**
     * Получить группу по UUID
     *
     * @param groupUuid UUID группы
     * @return Группа {@link Group}
     * @throws GroupNotFoundException       если группа с указанным UUID не найдена
     * @throws ConstraintViolationException если groupUuid == null
     */
    Group findById(@NotNull(message = "{group.uuid.required}") UUID groupUuid);
}
