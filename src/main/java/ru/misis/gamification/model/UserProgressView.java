package ru.misis.gamification.model;

/**
 * Модель прогресса пользователя
 *
 * @param userId            Идентификатор пользователя из LMS
 * @param totalPoints       Общее количество накопленных очков
 * @param level             Текущий уровень
 * @param pointsToNextLevel Количество очков, необходимых для достижения следующего уровня
 * @param progressPercent   Процент заполнения текущего уровня
 */
public record UserProgressView(
        String userId,
        Integer totalPoints,
        Integer level,
        Long pointsToNextLevel,
        Double progressPercent
) {
}
