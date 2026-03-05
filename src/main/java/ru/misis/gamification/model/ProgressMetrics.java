package ru.misis.gamification.model;

/**
 * Метрики прогресса пользователя для отображения в виджете и админ-панели
 *
 * @param pointsToNextLevel Количество очков, необходимых для достижения следующего уровня
 * @param progressPercent   Процент заполнения текущего уровня
 */
public record ProgressMetrics(long pointsToNextLevel, double progressPercent) {
}
