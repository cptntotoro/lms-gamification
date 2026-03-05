package ru.misis.gamification.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель транзакции
 *
 * @param uuid        UUID транзакции
 * @param userId      Идентификатор пользователя из LMS
 * @param eventId     Идентификатор события из LMS
 * @param points      Количество очков
 * @param description Описание
 * @param createdAt   Дата и время создания
 */
public record TransactionSummary(
        UUID uuid,
        String userId,
        String eventId,
        Integer points,
        String description,
        LocalDateTime createdAt
) {
}
