package ru.misis.gamification.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель типа события
 *
 * @param uuid           UUID типа события
 * @param typeCode       Уникальный код типа события из LMS
 * @param displayName    Название типа события
 * @param points         Количество очков, начисляемых за одно событие этого типа
 * @param maxDailyPoints Максимальное количество очков в сутки по этому типу (null = без ограничения)
 * @param active         Флаг активности типа события
 * @param createdAt      Дата и время создания
 * @param updatedAt      Дата и время обновления
 */
public record EventTypeSummary(
        UUID uuid,
        String typeCode,
        String displayName,
        Integer points,
        Integer maxDailyPoints,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
