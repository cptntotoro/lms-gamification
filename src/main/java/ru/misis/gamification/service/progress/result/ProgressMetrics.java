//package ru.misis.gamification.service.progress.result;
//
//import io.swagger.v3.oas.annotations.media.Schema;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//
/// **
// * Метрики прогресса пользователя для отображения в виджете и админ-панели
// */
//@Getter
//@AllArgsConstructor
//@Schema(description = "Рассчитанные метрики прогресса пользователя")
//public class ProgressMetrics {
//
//    /**
//     * Количество очков, необходимых для достижения следующего уровня.
//     * <p>Значение всегда положительное (> 0).</p>
//     */
//    private final long pointsToNextLevel;
//
//    /**
//     * Процент заполнения текущего уровня (0–100).
//     * <p>Если pointsToNextLevel = 0 — возвращается 100.0.</p>
//     */
//    private final double progressPercent;
//}