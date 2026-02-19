package ru.misis.gamification.service.event.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.exception.DuplicateEventTypeException;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.model.admin.EventType;

import java.util.UUID;

/**
 * Сервис для работы с типами событий {@link EventType} для администратора
 */
public interface EventTypeAdminService {

    /**
     * Создать тип события из LMS
     *
     * @param eventType Тип события из LMS
     * @return Тип события из LMS
     * @throws DuplicateEventTypeException если тип события с таким кодом уже существует
     */
    EventType create(EventType eventType);

    /**
     * Получить тип события из LMS
     *
     * @param uuid Идентификатор записи в таблице
     * @return Тип события из LMS
     * @throws EventTypeNotFoundException если тип события не найден
     */
    EventType getById(UUID uuid);

    /**
     * Обновить тип события из LMS
     *
     * @param uuid Идентификатор записи в таблице
     * @param eventType Тип события из LMS
     * @return Тип события из LMS
     */
    EventType update(UUID uuid, EventType eventType);

    /**
     * Деактивировать тип события из LMS
     *
     * @param uuid Идентификатор записи в таблице
     */
    void deactivate(UUID uuid);

    /**
     * Получить страницу типов событий из LMS
     *
     * @param pageable Параметры пагинации и сортировки
     * @return Страница типов событий из LMS
     */
    Page<EventType> findAll(Pageable pageable);
}
