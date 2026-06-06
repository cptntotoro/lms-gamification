package ru.misis.gamification.service.simple.progress.processors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LinearLevelCalculatorProcessor implements LevelCalculatorProcessor {
    /**
     * Базовый множитель для расчёта очков.
     * <p>Используется во всех моделях как стартовая единица.</p>
     */
    @Value("${gamification.features.leveling.formula.base:500}")
    private int base;

    /**
     * Прирост очков между уровнями для линейной модели.
     * <p>Используется только в модели {@code LINEAR}.</p>
     */
    @Value("${gamification.features.leveling.formula.increment:200}")
    private int increment;


    /**
     * Расчёт уровня по квадратичной модели
     * <p>
     * Формула: L = ⌊ √(total / base) ⌋ + 1
     * </p>
     *
     * @param total Накопленные очки
     * @return Рассчитанный уровень (≥ 1)
     */
    @Override
    public int calculateLevel(int total) {
        if (total <= 0) return 1;
        double n = Math.sqrt(total / (double) base);
        return Math.max(1, (int) Math.floor(n) + 1);
    }


    /**
     * Расчёт очков до следующего уровня по линейной модели
     *
     * @param level Текущий уровень
     * @return Очки до уровня level + 1
     */
    @Override
    public long pointsToNextLevel(int level) {
        return base + (long) increment * level;
    }

    @Override
    public LevelCalculatorProcessorName getLevelCalculatorProcessorName() {
        return LevelCalculatorProcessorName.LINEAR;
    }
}
