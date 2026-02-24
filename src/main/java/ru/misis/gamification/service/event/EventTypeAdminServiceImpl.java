package ru.misis.gamification.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.exception.DuplicateEventTypeException;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.repository.EventTypeRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventTypeAdminServiceImpl implements EventTypeAdminService {

    /**
     * Репозиторий типов событий
     */
    private final EventTypeRepository repository;

    @Transactional
    @Override
    public EventType create(EventType eventType) {
        if (repository.existsByTypeCode(eventType.getTypeCode())) {
            throw new DuplicateEventTypeException(eventType.getTypeCode());
        }
        return repository.save(eventType);
    }

    @Transactional(readOnly = true)
    @Override
    public EventType getById(UUID uuid) {
        return repository.findById(uuid)
                .orElseThrow(() -> new EventTypeNotFoundException(uuid));
    }

    @Transactional
    @Override
    public EventType update(UUID id, EventType updated) {
        EventType existing = getById(id);

        // Обновляем только разрешённые поля
        if (updated.getDisplayName() != null) {
            existing.setDisplayName(updated.getDisplayName());
        }
        if (updated.getPoints() != null) {
            existing.setPoints(updated.getPoints());
        }
        if (updated.getMaxDailyPoints() != null) {
            existing.setMaxDailyPoints(updated.getMaxDailyPoints());
        }
        if (updated.isActive() != existing.isActive()) {
            existing.setActive(updated.isActive());
        }

        return repository.save(existing);
    }

    @Transactional
    @Override
    public void deactivate(UUID id) {
        EventType type = getById(id);
        type.setActive(false);
        repository.save(type);
        log.info("Тип события деактивирован: code={}, id={}", type.getTypeCode(), id);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<EventType> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
