package ru.misis.gamification.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.repository.EventTypeRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventTypeServiceImpl implements EventTypeService {

    /**
     * Репозиторий типов событий
     */
    private final EventTypeRepository eventTypeRepository;

    @Transactional(readOnly = true)
    @Override
    public EventType getActiveByCode(String typeCode) {
        return eventTypeRepository.findByTypeCodeAndActiveTrue(typeCode)
                .orElseThrow(() -> new EventTypeNotFoundException(
                        "Активный тип события не найден по коду: " + typeCode));
    }

    @Transactional(readOnly = true)
    @Override
    public boolean canAwardPoints(String userId, String typeCode, int pointsToAward, LocalDate date) {
        EventType type = getActiveByCode(typeCode);
        if (type.getMaxDailyPoints() == null) {
            return true;
        }

        long current = eventTypeRepository.sumPointsByUserIdAndEventTypeAndDate(userId, typeCode, date);
        return current + pointsToAward <= type.getMaxDailyPoints();
    }

    /**
     * Сумма очков, начисленных пользователю по типу события за день.
     */
    @Transactional(readOnly = true)
    @Override
    public long getDailyPointsSum(String userId, String typeCode, LocalDate date) {
        return eventTypeRepository.sumPointsByUserIdAndEventTypeAndDate(userId, typeCode, date);
    }
}
