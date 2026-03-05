package ru.misis.gamification.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель пользователя для администратора
 *
 * @param uuid              UUID пользователя
 * @param userId            Идентификатор пользователя из LMS
 * @param totalPoints       Общее количество накопленных очков
 * @param level             Текущий уровень
 * @param pointsToNextLevel Количество очков, необходимых для достижения следующего уровня
 * @param progressPercent   Процент заполнения текущего уровня
 * @param createdAt         Дата и время создания
 * @param updatedAt         Дата и время обновления
 */
public record UserAdminView(
        UUID uuid,
        String userId,
        Integer totalPoints,
        Integer level,
        Long pointsToNextLevel,
        Double progressPercent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
