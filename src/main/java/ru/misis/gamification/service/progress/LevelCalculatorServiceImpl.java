package ru.misis.gamification.service.progress;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@ConfigurationProperties(prefix = "leveling")
@Data
public class LevelCalculatorServiceImpl implements LevelCalculatorService {

    /**
     * Тип формулы расчёта уровня.
     * <p>Возможные значения: {@code TRIANGULAR}, {@code QUADRATIC}, {@code LINEAR} (регистр не важен).</p>
     * <p>По умолчанию: {@code TRIANGULAR}.</p>
     */
    private String formula = "TRIANGULAR";

    /**
     * Базовый множитель для расчёта очков.
     * <p>Используется во всех моделях как стартовая единица.</p>
     * <p>По умолчанию: 500.</p>
     */
    private int base = 500;

    /**
     * Прирост очков между уровнями для линейной модели.
     * <p>Используется только в модели {@code LINEAR}.</p>
     * <p>По умолчанию: 200.</p>
     */
    private int increment = 200;

    @Override
    public int calculateLevel(int totalPoints) {
        return switch (formula.toUpperCase()) {
            case "TRIANGULAR" -> calculateTriangular(totalPoints);
            case "QUADRATIC" -> calculateQuadratic(totalPoints);
            case "LINEAR" -> calculateLinear(totalPoints);
            default -> 1 + (totalPoints / 1000);
        };
    }

    @Override
    public long pointsToNextLevel(int currentLevel) {
        return switch (formula.toUpperCase()) {
            case "TRIANGULAR" -> calculateTriangularToNext(currentLevel);
            case "QUADRATIC" -> calculateQuadraticToNext(currentLevel);
            case "LINEAR" -> calculateLinearToNext(currentLevel);
            default -> 1000L;
        };
    }

    /**
     * Расчёт уровня по треугольной модели
     * <p>
     * Формула: L = ⌊ (-1 + √(1 + 8 × total / base)) / 2 ⌋ + 1
     * </p>
     *
     * @param total Накопленные очки
     * @return Рассчитанный уровень (≥ 1)
     */
    private int calculateTriangular(int total) {
        if (total <= 0) return 1;
        double n = (-1 + Math.sqrt(1 + 8.0 * total / base)) / 2;
        return Math.max(1, (int) Math.floor(n) + 1);
    }

    /**
     * Расчёт уровня по квадратичной модели
     * <p>
     * Формула: L = ⌊ √(total / base) ⌋ + 1
     * </p>
     *
     * @param total Накопленные очки
     * @return Рассчитанный уровень (≥ 1)
     */
    private int calculateQuadratic(int total) {
        if (total <= 0) return 1;
        double n = Math.sqrt(total / (double) base);
        return Math.max(1, (int) Math.floor(n) + 1);
    }

    /**
     * Расчёт уровня по линейной модели
     * <p>
     * Итеративно вычисляет кумулятивную сумму: base + increment × (0 + 1 + ... + (level-1))
     * </p>
     *
     * @param total Накопленные очки
     * @return Рассчитанный уровень (≥ 1)
     */
    private int calculateLinear(int total) {
        if (total <= 0) return 1;
        int level = 1;
        int sum = 0;
        while (sum <= total) {
            sum += base + increment * (level - 1);
            level++;
        }
        return level - 1;
    }

    /**
     * Расчёт очков до следующего уровня по треугольной модели
     *
     * @param level Текущий уровень
     * @return Очки до уровня level + 1
     */
    private long calculateTriangularToNext(int level) {
        long current = (long) base * level * (level + 1) / 2;
        long next = (long) base * (level + 1) * (level + 2) / 2;
        return next - current;
    }

    /**
     * Расчёт очков до следующего уровня по квадратичной модели
     *
     * @param level Текущий уровень
     * @return Очки до уровня level + 1
     */
    private long calculateQuadraticToNext(int level) {
        long current = (long) base * level * level;
        long next = (long) base * (level + 1) * (level + 1);
        return next - current;
    }

    /**
     * Расчёт очков до следующего уровня по линейной модели
     *
     * @param level Текущий уровень
     * @return Очки до уровня level + 1
     */
    private long calculateLinearToNext(int level) {
        return base + (long) increment * level;
    }
}