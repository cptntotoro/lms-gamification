package ru.misis.gamification.service.progress.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Метрики прогресса пользователя для отображения в виджете и админ-панели.
 * <p>
 * Этот класс содержит только рассчитанные значения:
 * <ul>
 *     <li>очки до следующего уровня</li>
 *     <li>процент заполнения текущего уровня (0–100)</li>
 * </ul>
 * </p>
 * <p>
 * Класс иммутабельный (все поля final), предназначен исключительно для передачи данных
 * между слоями (сервис → контроллер → шаблон / ответ API).
 * </p>
 *
 * @since 0.1.0 (февраль 2026)
 */
@Getter
@AllArgsConstructor
@Schema(description = "Рассчитанные метрики прогресса пользователя")
public class ProgressMetrics {

    /**
     * Количество очков, необходимых для достижения следующего уровня.
     * <p>Значение всегда положительное (> 0).</p>
     */
    private final long pointsToNextLevel;

    /**
     * Процент заполнения текущего уровня (0–100).
     * <p>Если pointsToNextLevel = 0 — возвращается 100.0.</p>
     */
    private final double progressPercent;
}