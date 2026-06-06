package ru.misis.gamification.service.simple.progress;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.misis.gamification.service.simple.progress.processors.LevelCalculatorProcessor;
import ru.misis.gamification.service.simple.progress.processors.LevelCalculatorProcessorName;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LevelCalculatorServiceImpl implements LevelCalculatorService {

    /**
     * Тип формулы расчёта уровня.
     * <p>Возможные значения: {@code TRIANGULAR}, {@code QUADRATIC}, {@code LINEAR} (регистр не важен).</p>
     */
    @Value("${gamification.features.leveling.formula.type:TRIANGULAR}")
    private String formula;

    private Map<LevelCalculatorProcessorName, LevelCalculatorProcessor> processors;

    @Autowired
    void setProcessors(Set<LevelCalculatorProcessor> processors) {
        this.processors = processors.stream().collect(Collectors.toMap(LevelCalculatorProcessor::getLevelCalculatorProcessorName, processor -> processor));
    }

    private LevelCalculatorProcessor getProcessor() {
        LevelCalculatorProcessorName processorName = LevelCalculatorProcessorName.valueOf(formula.toUpperCase());
        LevelCalculatorProcessor processor = processors.get(processorName);
        if (null == processor) {
            log.warn("LevelCalculatorProcessor with name=" + processorName + " doesn't exist!");
            processor = processors.get(LevelCalculatorProcessorName.FALLBACK);
        }
        return processor;
    }

    @Override
    public int calculateLevel(int totalPoints) {
        if (totalPoints <= 0) {
            return 1;
        }
        return getProcessor().calculateLevel(totalPoints);
    }

    @Override
    public long pointsToNextLevel(int currentLevel) {
        if (currentLevel < 1) {
            return pointsToNextLevel(1);
        }
        return getProcessor().pointsToNextLevel(currentLevel);
    }
}
