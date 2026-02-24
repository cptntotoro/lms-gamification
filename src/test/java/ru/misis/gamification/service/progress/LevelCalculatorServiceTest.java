package ru.misis.gamification.service.progress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class LevelCalculatorServiceTest {

    private LevelCalculatorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new LevelCalculatorServiceImpl();
    }

    @ParameterizedTest(name = "TRIANGULAR: {0} очков → уровень {1}")
    @CsvSource({
            "0,     1",
            "1,     1",
            "499,   1",
            "500,   2",
            "1499,  2",
            "1500,  3",
            "2999,  3",
            "3000,  4",
            // Крупные значения
            "124750,  22",
            "127500,  23"
    })
    void calculateLevel_triangular_returnsCorrectLevel(int totalPoints, int expectedLevel) {
        service.setFormula("TRIANGULAR");
        assertThat(service.calculateLevel(totalPoints)).isEqualTo(expectedLevel);
    }

    @ParameterizedTest(name = "QUADRATIC: {0} очков → уровень {1}")
    @CsvSource({
            "0,     1",
            "499,   1",
            "500,   2",
            "1999,  2",
            "2000,  3",
            "4499,  3",
            "4500,  4",
            // Крупные значения
            "1247500,  50",
            "1322500,  52"
    })
    void calculateLevel_quadratic_returnsCorrectLevel(int totalPoints, int expectedLevel) {
        service.setFormula("QUADRATIC");
        assertThat(service.calculateLevel(totalPoints)).isEqualTo(expectedLevel);
    }

    @ParameterizedTest(name = "LINEAR: {0} очков → уровень {1}")
    @CsvSource({
            "0,     1",
            "499,   1",
            "500,   2",
            "1199,  2",
            "1200,  3",
            "2099,  3",
            "2100,  4",
            // Крупные значения
            "124750,  34",
            "127500,  34"
    })
    @DisplayName("LINEAR формула: корректный расчёт уровня")
    void calculateLevel_linear_returnsCorrectLevel(int totalPoints, int expectedLevel) {
        service.setFormula("LINEAR");
        service.setBase(500);
        service.setIncrement(200);
        assertThat(service.calculateLevel(totalPoints)).isEqualTo(expectedLevel);
    }

    @Test
    void pointsToNextLevel_triangular() {
        service.setFormula("TRIANGULAR");
        service.setBase(500);

        assertThat(service.pointsToNextLevel(1)).isEqualTo(1000L);   // 1→2: 1000
        assertThat(service.pointsToNextLevel(2)).isEqualTo(1500L);   // 2→3: 1500
        assertThat(service.pointsToNextLevel(3)).isEqualTo(2000L);   // 3→4: 2000
        assertThat(service.pointsToNextLevel(50)).isEqualTo(25500L); // 50→51
    }

    @Test
    void pointsToNextLevel_quadratic() {
        service.setFormula("QUADRATIC");
        service.setBase(500);

        assertThat(service.pointsToNextLevel(1)).isEqualTo(1500L);   // 1→2: 500*(4-1)
        assertThat(service.pointsToNextLevel(2)).isEqualTo(2500L);   // 2→3: 500*(9-4)
        assertThat(service.pointsToNextLevel(3)).isEqualTo(3500L);   // 3→4
        assertThat(service.pointsToNextLevel(50)).isEqualTo(50500L); // 50→51
    }

    @Test
    void pointsToNextLevel_linear() {
        service.setFormula("LINEAR");
        service.setBase(500);
        service.setIncrement(200);

        assertThat(service.pointsToNextLevel(1)).isEqualTo(700L);    // 500 + 200*1
        assertThat(service.pointsToNextLevel(2)).isEqualTo(900L);    // 500 + 200*2
        assertThat(service.pointsToNextLevel(3)).isEqualTo(1100L);
        assertThat(service.pointsToNextLevel(50)).isEqualTo(10500L); // 500 + 200*50
    }

    @Test
    void calculateLevel_negativePoints_returnsLevel1() {
        service.setFormula("TRIANGULAR");
        assertThat(service.calculateLevel(-100)).isEqualTo(1);
    }

    @Test
    void calculateLevel_unknownFormula_usesFallback() {
        service.setFormula("UNKNOWN");
        assertThat(service.calculateLevel(0)).isEqualTo(1);
        assertThat(service.calculateLevel(999)).isEqualTo(1);
        assertThat(service.calculateLevel(1000)).isEqualTo(2);
        assertThat(service.calculateLevel(5000)).isEqualTo(6);
    }

    @Test
    void configurationProperties_areApplied() {
        service.setFormula("LINEAR");
        service.setBase(100);
        service.setIncrement(50);

        assertThat(service.calculateLevel(0)).isEqualTo(1);
        assertThat(service.calculateLevel(99)).isEqualTo(1);
        assertThat(service.calculateLevel(100)).isEqualTo(2);
        assertThat(service.pointsToNextLevel(1)).isEqualTo(150L); // 100 + 50*1
    }
}