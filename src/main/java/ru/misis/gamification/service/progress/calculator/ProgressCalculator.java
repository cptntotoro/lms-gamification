package ru.misis.gamification.service.progress.calculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.service.progress.LevelCalculatorService;
import ru.misis.gamification.service.progress.result.ProgressMetrics;

/**
 * Компонент расчёта метрик прогресса пользователя
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProgressCalculator {

    /**
     * Сервис расчета уровня пользователя на основе накопленных очков
     */
    private final LevelCalculatorService levelCalculatorService;

    /**
     * Рассчитывает метрики прогресса на основе текущего состояния пользователя.
     *
     * @param user пользователь с актуальными totalPoints и level
     * @return объект с рассчитанными метриками (никогда null)
     * @throws IllegalArgumentException если user null
     */
    public ProgressMetrics calculate(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }

        Integer userLevel = user.getLevel();
        Integer totalPoints = user.getTotalPoints();

        int points = totalPoints != null ? totalPoints : 0;
        int level = userLevel != null ? userLevel : 1;

        long pointsToNextLevel = levelCalculatorService.pointsToNextLevel(level);
        double progressPercent = pointsToNextLevel > 0 ? Math.min((double) points / pointsToNextLevel * 100, 100) : 100.0;

        log.trace("Расчёт прогресса: userId={}, level={}, totalPoints={}, pointsToNextLevel={}, progressPercent={}%",
                user.getUserId(), userLevel, totalPoints, pointsToNextLevel, progressPercent);

        return new ProgressMetrics(pointsToNextLevel, progressPercent);
    }
}