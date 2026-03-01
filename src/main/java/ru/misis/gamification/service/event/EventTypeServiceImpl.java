package ru.misis.gamification.service.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.repository.EventTypeRepository;
import ru.misis.gamification.service.user.UserService;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventTypeServiceImpl implements EventTypeService {

    /**
     * Репозиторий типов событий
     */
    private final EventTypeRepository eventTypeRepository;

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    @Transactional(readOnly = true)
    @Override
    public EventType getActiveByCode(@NotBlank(message = "{eventType.code.required}") String typeCode) {
        return eventTypeRepository.findByTypeCodeAndActiveTrue(typeCode)
                .orElseThrow(() -> new EventTypeNotFoundException(
                        "Активный тип события не найден по коду: " + typeCode));
    }

    @Transactional(readOnly = true)
    @Override
    public boolean canAwardPoints(@NotBlank(message = "{user.id.required}") String userId,
                                  @NotBlank(message = "{eventType.code.required}") String typeCode,
                                  @Min(value = 0, message = "{points.positive}") int pointsToAward,
                                  @NotNull(message = "{date.required}") LocalDate date) {
        EventType type = getActiveByCode(typeCode);

        if (type.getMaxDailyPoints() == null) {
            return true;
        }

        UUID userUuid = getUserUuid(userId);
        UUID eventTypeUuid = type.getUuid();

        long currentPoints = eventTypeRepository.calculateDailyPointsSumForUserAndType(
                userUuid, eventTypeUuid, date
        );

        return currentPoints + pointsToAward <= type.getMaxDailyPoints();
    }

    @Transactional(readOnly = true)
    @Override
    public long getDailyPointsSum(@NotBlank(message = "{user.id.required}") String userId,
                                  @NotBlank(message = "{eventType.code.required}") String typeCode,
                                  @NotNull(message = "{date.required}") LocalDate date) {
        UUID userUuid = getUserUuid(userId);
        EventType type = getActiveByCode(typeCode);

        return eventTypeRepository.calculateDailyPointsSumForUserAndType(
                userUuid, type.getUuid(), date
        );
    }

    private UUID getUserUuid(String userId) {
        return userService.getUserUuidByExternalId(userId);
    }
}
