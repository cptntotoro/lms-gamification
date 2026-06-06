package ru.misis.gamification.service.simple.progress.processors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.misis.gamification.service.simple.progress.LevelCalculatorService;

@Service
public class TriangularLevelCalculatorProcessor implements LevelCalculatorProcessor {
    /**
     * Базовый множитель для расчёта очков.
     * <p>Используется во всех моделях как стартовая единица.</p>
     */
    @Value("${gamification.features.leveling.formula.base:500}")
    private int base;


    /**
     * Расчёт уровня по треугольной модели
     * <p>
     * Формула: L = ⌊ (-1 + √(1 + 8 × total / base)) / 2 ⌋ + 1
     * </p>
     *
     * @param total Накопленные очки
     * @return Рассчитанный уровень (≥ 1)
     */
    @Override
    public int calculateLevel(int total) {
        if (total <= 0) return 1;
        double n = (-1 + Math.sqrt(1 + 8.0 * total / base)) / 2;
        return Math.max(1, (int) Math.floor(n) + 1);
    }


    /**
     * Расчёт очков до следующего уровня по треугольной модели
     *
     * @param level Текущий уровень
     * @return Очки до уровня level + 1
     */
    @Override
    public long pointsToNextLevel(int level) {
        long current = (long) base * level * (level + 1) / 2;
        long next = (long) base * (level + 1) * (level + 2) / 2;
        return next - current;
    }

    @Override
    public LevelCalculatorProcessorName getLevelCalculatorProcessorName() {
        return LevelCalculatorProcessorName.TRIANGULAR;
    }
}
