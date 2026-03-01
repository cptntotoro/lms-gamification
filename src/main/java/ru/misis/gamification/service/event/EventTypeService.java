package ru.misis.gamification.service.event;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.exception.EventTypeNotFoundException;

import java.time.LocalDate;

/**
 * Сервис управления типами событий {@link EventType}
 */
public interface EventTypeService {

    /**
     * Получить активный тип события по коду
     *
     * @param typeCode Уникальный код типа события из LMS
     * @return Тип события из LMS {@link EventType}
     * @throws EventTypeNotFoundException   если активный тип с указанным кодом не найден
     * @throws ConstraintViolationException если typeCode == null или пустая строка
     */
    EventType getActiveByCode(@NotBlank(message = "{eventType.code.required}") String typeCode);

    /**
     * Проверить, не превышен ли дневной лимит очков по типу для пользователя
     *
     * @param userId        Идентификатор пользователя из LMS
     * @param typeCode      Уникальный код типа события из LMS
     * @param pointsToAward Количество очков для начисления
     * @param date          Дата, для которой проверяется лимит
     * @return Да / Нет
     * @throws EventTypeNotFoundException   если активный тип события с указанным кодом не найден
     * @throws ConstraintViolationException если:
     *                                      <ul>
     *                                          <li>userId == null или пустая строка</li>
     *                                          <li>typeCode == null или пустая строка</li>
     *                                          <li>pointsToAward < 0</li>
     *                                          <li>date == null</li>
     *                                      </ul>
     */
    boolean canAwardPoints(@NotBlank(message = "{user.id.required}") String userId,
                           @NotBlank(message = "{eventType.code.required}") String typeCode,
                           @Min(value = 0, message = "{points.positive}") int pointsToAward,
                           @NotNull(message = "{date.required}") LocalDate date);

    /**
     * Получить сумму очков, уже начисленных пользователю по указанному типу события за конкретный день.
     *
     * @param userId   Идентификатор пользователя из LMS
     * @param typeCode Уникальный код типа события из LMS
     * @param date     Дата, за которую считается сумма
     * @return Сумма начисленных очков
     * @throws EventTypeNotFoundException   если активный тип события с указанным кодом не найден
     * @throws ConstraintViolationException если:
     *                                      <ul>
     *                                          <li>userId == null или пустая строка</li>
     *                                          <li>typeCode == null или пустая строка</li>
     *                                          <li>date == null</li>
     *                                      </ul>
     */
    long getDailyPointsSum(@NotBlank(message = "{user.id.required}") String userId,
                           @NotBlank(message = "{eventType.code.required}") String typeCode,
                           @NotNull(message = "{date.required}") LocalDate date);
}
