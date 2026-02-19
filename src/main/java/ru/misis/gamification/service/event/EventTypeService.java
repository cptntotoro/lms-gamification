package ru.misis.gamification.service.event;

import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.model.admin.EventType;

import java.time.LocalDate;

/**
 * Сервис для работы с типами событий {@link EventType}
 * Отвечает за поиск, проверку лимитов и получение эффективного количества очков
 */
public interface EventTypeService {

    /**
     * Находит активный тип события по коду
     *
     * @throws EventTypeNotFoundException если тип не найден или отключён
     */
    EventType getActiveByCode(String typeCode);

    /**
     * Проверяет, не превышен ли дневной лимит очков по типу для пользователя
     *
     * @return true, если можно начислить указанное количество очков
     */
    boolean canAwardPoints(String userId, String typeCode, int pointsToAward, LocalDate date);

    /**
     * Сумма очков, начисленных пользователю по типу события за день
     */
    long getDailyPointsSum(String userId, String typeCode, LocalDate date);
}
