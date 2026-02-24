package ru.misis.gamification.service.level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.service.progress.LevelCalculatorServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LevelCalculatorServiceTest {

    @InjectMocks
    private LevelCalculatorServiceImpl calculator;

    @BeforeEach
    void setUp() {
        calculator = new LevelCalculatorServiceImpl();
    }

    @Test
    void calculateLevel_Triangular_shouldReturnCorrectLevel() {
        calculator.setFormula("TRIANGULAR");
        calculator.setBase(1000);

        assertThat(calculator.calculateLevel(0)).isEqualTo(1);
        assertThat(calculator.calculateLevel(999)).isEqualTo(1);
        assertThat(calculator.calculateLevel(1000)).isEqualTo(2);
        assertThat(calculator.calculateLevel(2999)).isEqualTo(2);
        assertThat(calculator.calculateLevel(3000)).isEqualTo(3);
        assertThat(calculator.calculateLevel(5999)).isEqualTo(3);
        assertThat(calculator.calculateLevel(6000)).isEqualTo(4);
        assertThat(calculator.calculateLevel(9999)).isEqualTo(4);
        assertThat(calculator.calculateLevel(10000)).isEqualTo(5);
    }

    @Test
    void calculateLevel_Quadratic_shouldReturnCorrectLevel() {
        calculator.setFormula("QUADRATIC");
        calculator.setBase(200);

        assertThat(calculator.calculateLevel(0)).isEqualTo(1);
        assertThat(calculator.calculateLevel(199)).isEqualTo(1);
        assertThat(calculator.calculateLevel(200)).isEqualTo(2);
        assertThat(calculator.calculateLevel(799)).isEqualTo(2);
        assertThat(calculator.calculateLevel(800)).isEqualTo(3);
        assertThat(calculator.calculateLevel(1799)).isEqualTo(3);
        assertThat(calculator.calculateLevel(1800)).isEqualTo(4);
        assertThat(calculator.calculateLevel(4999)).isEqualTo(5);
        assertThat(calculator.calculateLevel(5000)).isEqualTo(6);
    }

    @Test
    void calculateLevel_Linear_shouldReturnCorrectLevel() {
        calculator.setFormula("LINEAR");
        calculator.setBase(1000);
        calculator.setIncrement(200);

        assertThat(calculator.calculateLevel(0)).isEqualTo(1);
        assertThat(calculator.calculateLevel(999)).isEqualTo(1);
        assertThat(calculator.calculateLevel(1000)).isEqualTo(2);
        assertThat(calculator.calculateLevel(2199)).isEqualTo(2);
        assertThat(calculator.calculateLevel(2200)).isEqualTo(3);
        assertThat(calculator.calculateLevel(3599)).isEqualTo(3);
        assertThat(calculator.calculateLevel(3600)).isEqualTo(4);
    }

    @Test
    void calculateLevel_unknownFormula_shouldUseFallback() {
        calculator.setFormula("UNKNOWN");

        assertThat(calculator.calculateLevel(0)).isEqualTo(1);
        assertThat(calculator.calculateLevel(999)).isEqualTo(1);
        assertThat(calculator.calculateLevel(1000)).isEqualTo(2);
        assertThat(calculator.calculateLevel(1999)).isEqualTo(2);
        assertThat(calculator.calculateLevel(9999)).isEqualTo(10);
    }

    @ParameterizedTest
    @CsvSource({
            "TRIANGULAR, 1000, 1, 2000",
            "TRIANGULAR, 1000, 2, 3000",
            "TRIANGULAR, 1000, 3, 4000",
            "TRIANGULAR, 1000, 10, 11000",

            "QUADRATIC, 200, 1, 600",
            "QUADRATIC, 200, 2, 1000",
            "QUADRATIC, 200, 3, 1400",

            "LINEAR, 1000, 1, 1200",
            "LINEAR, 1000, 2, 1400",
            "LINEAR, 1000, 5, 2000"
    })
    void pointsToNextLevel_shouldReturnCorrectValue(String formula, int base, int currentLevel, long expected) {
        calculator.setFormula(formula);
        calculator.setBase(base);
        calculator.setIncrement(200);

        assertThat(calculator.pointsToNextLevel(currentLevel)).isEqualTo(expected);
    }

    @Test
    void pointsToNextLevel_unknownFormula_shouldReturnDefault1000() {
        calculator.setFormula("UNKNOWN");

        assertThat(calculator.pointsToNextLevel(1)).isEqualTo(1000);
        assertThat(calculator.pointsToNextLevel(10)).isEqualTo(1000);
        assertThat(calculator.pointsToNextLevel(50)).isEqualTo(1000);
    }

    @Test
    void configurationProperties_shouldWork() {
        calculator.setFormula("QUADRATIC");
        calculator.setBase(300);

        assertThat(calculator.getFormula()).isEqualTo("QUADRATIC");
        assertThat(calculator.getBase()).isEqualTo(300);

        // 300 * 3² = 2700 → уровень 4 (потому что Math.floor(sqrt(2700/300)) + 1 = Math.floor(sqrt(9)) + 1 = 3 + 1 = 4
        assertThat(calculator.calculateLevel(2700)).isEqualTo(4);
    }

    @Test
    void calculateLevel_negativePoints_shouldReturnLevel1() {
        calculator.setFormula("TRIANGULAR");
        assertThat(calculator.calculateLevel(-100)).isEqualTo(1);

        calculator.setFormula("QUADRATIC");
        assertThat(calculator.calculateLevel(-100)).isEqualTo(1);

        calculator.setFormula("LINEAR");
        assertThat(calculator.calculateLevel(-100)).isEqualTo(1);

        calculator.setFormula("UNKNOWN");
        assertThat(calculator.calculateLevel(-100)).isEqualTo(1);
    }

    @Test
    void calculateLevel_veryLargeNumber_shouldNotOverflow() {
        calculator.setFormula("TRIANGULAR");
        calculator.setBase(1000);

        assertThat(calculator.calculateLevel(Integer.MAX_VALUE)).isGreaterThan(1000);
    }
}