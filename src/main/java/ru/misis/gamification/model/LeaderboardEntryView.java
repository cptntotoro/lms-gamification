package ru.misis.gamification.model;

import java.util.UUID;

/**
 * Модель строки лидерборда
 *
 * @param userUuid       UUID пользователя
 * @param userId         Идентификатор пользователя из LMS
 * @param pointsInCourse Количество заработанных очков на курсе
 * @param globalLevel    Общий уровень пользователя
 * @param rank           Позиция в лидерборде
 * @param isCurrentUser  Флаг текущего пользователя
 */
public record LeaderboardEntryView(
        UUID userUuid,
        String userId,
        Integer pointsInCourse,
        Integer globalLevel,
        Long rank,
        Boolean isCurrentUser
) {
    // Конструктор без флага текущего пользователя (для топа)
    public LeaderboardEntryView(UUID userUuid, String userId, Integer pointsInCourse, Integer globalLevel, Long rank) {
        this(userUuid, userId, pointsInCourse, globalLevel, rank, false);
    }
}