package ru.misis.gamification.service.application.eventtype;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.model.EventTypeSummary;
import ru.misis.gamification.service.simple.eventtype.EventTypeAdminService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventTypeAdminApplicationServiceImpl implements EventTypeAdminApplicationService {

    private final EventTypeAdminService eventTypeAdminService;

    // TODO: добавить маппинг

    @Transactional
    @Override
    public EventTypeSummary create(EventType eventType) {
        EventType saved = eventTypeAdminService.create(eventType);
        log.info("Создан новый тип события: code={}", saved.getTypeCode());
        return toSummary(saved);
    }

    @Override
    public EventTypeSummary getById(UUID uuid) {
        EventType type = eventTypeAdminService.getById(uuid);
        return toSummary(type);
    }

    @Transactional
    @Override
    public EventTypeSummary update(UUID uuid, EventType eventType) {
        EventType updated = eventTypeAdminService.update(uuid, eventType);
        return toSummary(updated);
    }

    @Transactional
    @Override
    public void deactivate(UUID id) {
        eventTypeAdminService.deactivate(id);
        log.info("Тип события деактивирован: id={}", id);
    }

    @Override
    public Page<EventTypeSummary> findAll(Pageable pageable) {
        return eventTypeAdminService.findAll(pageable).map(this::toSummary);
    }

    private EventTypeSummary toSummary(EventType type) {
        return new EventTypeSummary(
                type.getUuid(),
                type.getTypeCode(),
                type.getDisplayName(),
                type.getPoints(),
                type.getMaxDailyPoints(),
                type.isActive(),
                type.getCreatedAt(),
                type.getUpdatedAt()
        );
    }
}
