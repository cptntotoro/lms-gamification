package ru.misis.gamification.service.application.eventtype;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.model.EventTypeSummary;

import java.util.UUID;

public interface EventTypeAdminApplicationService {

    /**
     * Создать тип события из LMS
     *
     * @param eventType Тип события из LMS
     * @return Модель типа события
     */
    EventTypeSummary create(EventType eventType);

    /**
     * Получить тип события из LMS по UUID
     *
     * @param eventTypeUuid UUID типа события
     * @return Модель типа события
     */
    EventTypeSummary getById(UUID eventTypeUuid);

    /**
     * Обновить тип события из LMS по UUID
     *
     * @param eventTypeUuid UUID типа события
     * @param eventType     Тип события из LMS
     * @return Модель типа события
     */
    EventTypeSummary update(UUID eventTypeUuid, EventType eventType);

    /**
     * Отключить тип события из LMS по UUID
     *
     * @param eventTypeUuid UUID типа события
     */
    void deactivate(UUID eventTypeUuid);

    /**
     * Получить страницу типов событий из LMS
     *
     * @param pageable Параметры пагинации и сортировки
     * @return Страница моделей типа события
     */
    Page<EventTypeSummary> findAll(Pageable pageable);
}
