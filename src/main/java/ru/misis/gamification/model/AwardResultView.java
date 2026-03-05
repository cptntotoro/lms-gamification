package ru.misis.gamification.model;

/**
 * Результат операции начисления очков по событию из LMS
 *
 * @param success           Флаг успеха
 * @param pointsEarned      Начисленные очки
 * @param totalPointsAfter  Новое общее количество очков
 * @param levelUp           Флаг повышения уровня
 * @param newLevel          Новый уровень
 * @param pointsToNextLevel Количество очков, необходимых для достижения следующего уровня
 * @param progressPercent   Процент заполнения текущего уровня
 * @param rejectionReason   Причина отказа в начислении
 * @param duplicate         Флаг дубликата события
 */
public record AwardResultView(boolean success, int pointsEarned, int totalPointsAfter, boolean levelUp, int newLevel,
                              long pointsToNextLevel, double progressPercent, String rejectionReason,
                              boolean duplicate
) {

}
