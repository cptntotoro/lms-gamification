package ru.misis.gamification.service.simple.progress.processors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FallbackLevelCalculatorProcessor implements LevelCalculatorProcessor {

    @Value("${gamification.features.leveling.formula.fallback-level-increment:1000}")
    private long fallbackIncrement;


    @Override
    public int calculateLevel(int total) {
        if (fallbackIncrement == 0) {
            return  1;
        }
        return 1 + (total / (int) fallbackIncrement);
    }


    @Override
    public long pointsToNextLevel(int level) {
        return fallbackIncrement == 0 ? 1000L : fallbackIncrement;
    }

    @Override
    public LevelCalculatorProcessorName getLevelCalculatorProcessorName() {
        return LevelCalculatorProcessorName.FALLBACK;
    }
}
