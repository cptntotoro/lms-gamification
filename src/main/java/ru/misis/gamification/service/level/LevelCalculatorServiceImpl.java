package ru.misis.gamification.service.level;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@ConfigurationProperties(prefix = "leveling")
@Data
public class LevelCalculatorServiceImpl implements LevelCalculatorService {

    private String formula = "TRIANGULAR";
    private int base = 500;
    private int increment = 200;

    @Override
    public int calculateLevel(int totalPoints) {
        return switch (formula.toUpperCase()) {
            case "TRIANGULAR" -> calculateTriangular(totalPoints);
            case "QUADRATIC" -> calculateQuadratic(totalPoints);
            case "LINEAR" -> calculateLinear(totalPoints);
            default -> 1 + (totalPoints / 1000); // fallback
        };
    }

    private int calculateTriangular(int total) {
        if (total <= 0) return 1;
        // n * (n + 1) / 2 * base ≈ total
        // решаем квадратное уравнение
        double n = (-1 + Math.sqrt(1 + 8.0 * total / base)) / 2;
        return Math.max(1, (int) Math.floor(n) + 1);
    }

    private int calculateQuadratic(int total) {
        if (total <= 0) return 1;
        // base * n² ≈ total
        double n = Math.sqrt(total / (double) base);
        return Math.max(1, (int) Math.floor(n) + 1);
    }

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

    @Override
    public long pointsToNextLevel(int currentLevel) {
        return switch (formula.toUpperCase()) {
            case "TRIANGULAR" -> calculateTriangularToNext(currentLevel);
            case "QUADRATIC" -> calculateQuadraticToNext(currentLevel);
            case "LINEAR" -> calculateLinearToNext(currentLevel);
            default -> 1000L;
        };
    }

    private long calculateTriangularToNext(int level) {
        long current = (long) base * level * (level + 1) / 2;
        long next = (long) base * (level + 1) * (level + 2) / 2;
        return next - current;
    }

    private long calculateQuadraticToNext(int level) {
        long current = (long) base * level * level;
        long next = (long) base * (level + 1) * (level + 1);
        return next - current;
    }

    private long calculateLinearToNext(int level) {
        return base + increment * level;
    }
}