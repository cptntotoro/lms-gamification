package ru.misis.gamification.service.simple.eventtype;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.repository.EventTypeRepository;
import ru.misis.gamification.service.simple.user.UserService;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class EventTypeServiceImpl implements EventTypeService {

    /**
     * Репозиторий типов событий
     */
    private final EventTypeRepository repository;

    /**
     * Сервис управления пользователями
     */
    private final UserService userSimpleService;

    @Override
    public EventType getActiveByCode(String typeCode) {
        return repository.findByTypeCodeAndActiveTrue(typeCode)
                .orElseThrow(() -> new EventTypeNotFoundException("Активный тип события не найден по коду: " + typeCode));
    }

    @Override
    public boolean canAwardPoints(String userId, String typeCode, int pointsToAward, LocalDate date) {
        EventType type = getActiveByCode(typeCode);
        if (type.getMaxDailyPoints() == null) return true;

        UUID userUuid = userSimpleService.getUserUuidByExternalId(userId);
        long currentPoints = repository.calculateDailyPointsSumForUserAndType(userUuid, type.getUuid(), date);
        return currentPoints + pointsToAward <= type.getMaxDailyPoints();
    }

    @Override
    public long getDailyPointsSum(String userId, String typeCode, LocalDate date) {
        UUID userUuid = userSimpleService.getUserUuidByExternalId(userId);
        EventType type = getActiveByCode(typeCode);
        return repository.calculateDailyPointsSumForUserAndType(userUuid, type.getUuid(), date);
    }
}
