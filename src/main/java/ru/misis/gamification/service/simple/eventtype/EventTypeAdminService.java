package ru.misis.gamification.service.simple.eventtype;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.exception.DuplicateEventTypeException;
import ru.misis.gamification.exception.EventTypeNotFoundException;

import java.util.UUID;

/**
 * Административный сервис для управления типами событий {@link EventType}
 */
public interface EventTypeAdminService {

    /**
     * Создать тип события из LMS
     *
     * @param eventType Тип события из LMS
     * @return Тип события из LMS {@link EventType}
     * @throws DuplicateEventTypeException  если тип события с таким кодом уже существует
     * @throws ConstraintViolationException если eventType == null
     */
    EventType create(@NotNull(message = "{eventType.required}") EventType eventType);

    /**
     * Получить тип события из LMS
     *
     * @param uuid Идентификатор записи в таблице
     * @return Тип события из LMS {@link EventType}
     * @throws EventTypeNotFoundException   если тип события не найден
     * @throws ConstraintViolationException если uuid == null
     */
    EventType getById(@NotNull(message = "{eventType.uuid.required}") UUID uuid);

    /**
     * Обновить тип события из LMS
     *
     * @param uuid      Идентификатор записи в таблице
     * @param eventType Тип события из LMS {@link EventType}
     * @return Тип события из LMS {@link EventType}
     * @throws EventTypeNotFoundException   если тип события с указанным UUID не найден
     * @throws ConstraintViolationException если id == null или updated == null
     */
    EventType update(@NotNull(message = "{eventType.uuid.required}") UUID uuid,
                     @NotNull(message = "{eventType.required}") EventType eventType);

    /**
     * Деактивировать тип события из LMS
     * <p>
     * После деактивации тип события перестаёт участвовать в обработке новых событий LMS.
     * Существующие начисления за прошлые события остаются без изменений.
     * </p>
     *
     * @param uuid Идентификатор записи в таблице
     * @throws EventTypeNotFoundException   если тип события с указанным UUID не найден
     * @throws ConstraintViolationException если uuid == null
     */
    void deactivate(@NotNull(message = "{eventType.uuid.required}") UUID uuid);

    /**
     * Получить страницу типов событий из LMS с поддержкой пагинации и сортировки
     *
     * @param pageable Параметры пагинации и сортировки
     * @return Страница типов событий из LMS {@link Page<EventType>}
     * @throws ConstraintViolationException если pageable == null
     */
    Page<EventType> findAll(@NotNull(message = "{pageable.required}") Pageable pageable);
}
