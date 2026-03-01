package ru.misis.gamification.service.event;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.exception.DuplicateEventTypeException;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.repository.EventTypeRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventTypeAdminServiceImpl implements EventTypeAdminService {

    /**
     * Репозиторий типов событий
     */
    private final EventTypeRepository repository;

    @Transactional
    @Override
    public EventType create(@NotNull(message = "{eventType.required}") EventType eventType) {
        if (repository.existsByTypeCode(eventType.getTypeCode())) {
            throw new DuplicateEventTypeException(eventType.getTypeCode());
        }
        return repository.save(eventType);
    }

    @Transactional(readOnly = true)
    @Override
    public EventType getById(@NotNull(message = "{eventType.uuid.required}") UUID uuid) {
        return repository.findById(uuid)
                .orElseThrow(() -> new EventTypeNotFoundException(uuid));
    }

    @Transactional
    @Override
    public EventType update(@NotNull(message = "{eventType.uuid.required}") UUID uuid,
                            @NotNull(message = "{eventType.required}") EventType eventType) {
        EventType existing = getById(uuid);

        if (eventType.getDisplayName() != null) {
            existing.setDisplayName(eventType.getDisplayName());
        }
        if (eventType.getPoints() != null) {
            existing.setPoints(eventType.getPoints());
        }
        if (eventType.getMaxDailyPoints() != null) {
            existing.setMaxDailyPoints(eventType.getMaxDailyPoints());
        }
        if (eventType.isActive() != existing.isActive()) {
            existing.setActive(eventType.isActive());
        }

        return repository.save(existing);
    }

    @Transactional
    @Override
    public void deactivate(@NotNull(message = "{eventType.uuid.required}") UUID id) {
        EventType type = getById(id);
        type.setActive(false);
        repository.save(type);
        log.info("Тип события деактивирован: code={}, id={}", type.getTypeCode(), id);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<EventType> findAll(@NotNull(message = "{pageable.required}") Pageable pageable) {
        return repository.findAll(pageable);
    }
}
