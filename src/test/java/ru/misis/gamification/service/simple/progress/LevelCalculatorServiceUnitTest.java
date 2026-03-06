package ru.misis.gamification.service.simple.progress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class LevelCalculatorServiceUnitTest {

    private LevelCalculatorServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new LevelCalculatorServiceImpl();
        setFormula("TRIANGULAR");
        setBase(500);
        setIncrement(200);
        setFallbackIncrement(1000L);
    }

    private void setFormula(String formula) throws Exception {
        Field field = LevelCalculatorServiceImpl.class.getDeclaredField("formula");
        field.setAccessible(true);
        field.set(service, formula);
    }

    private void setBase(int base) throws Exception {
        Field field = LevelCalculatorServiceImpl.class.getDeclaredField("base");
        field.setAccessible(true);
        field.set(service, base);
    }

    private void setIncrement(int increment) throws Exception {
        Field field = LevelCalculatorServiceImpl.class.getDeclaredField("increment");
        field.setAccessible(true);
        field.set(service, increment);
    }

    private void setFallbackIncrement(long value) throws Exception {
        Field field = LevelCalculatorServiceImpl.class.getDeclaredField("fallbackIncrement");
        field.setAccessible(true);
        field.set(service, value);
    }

    @ParameterizedTest
    @CsvSource({
            "TRIANGULAR, 0, 1",
            "TRIANGULAR, 499, 1",
            "TRIANGULAR, 500, 2",
            "TRIANGULAR, 1499, 2",
            "TRIANGULAR, 1500, 3",
            "QUADRATIC, 0, 1",
            "QUADRATIC, 499, 1",
            "QUADRATIC, 500, 2",
            "QUADRATIC, 1999, 2",
            "QUADRATIC, 2000, 3",
            "LINEAR, 0, 1",
            "LINEAR, 499, 1",
            "LINEAR, 500, 2",
            "LINEAR, 1199, 2",
            "LINEAR, 1200, 3",
            "CUSTOM_FORMULA, 0, 1",
            "CUSTOM_FORMULA, 1000, 2",
            "CUSTOM_FORMULA, 2000, 3"
    })
    void calculateLevel_returnsCorrectLevel(String formula, int points, int expected) throws Exception {
        setFormula(formula);
        if ("LINEAR".equalsIgnoreCase(formula)) {
            setIncrement(200);
        }
        assertThat(service.calculateLevel(points)).isEqualTo(expected);
    }

    @Test
    void calculateLevel_negativePoints_returns1() throws Exception {
        setFormula("TRIANGULAR");
        assertThat(service.calculateLevel(-100)).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({
            "TRIANGULAR, 1, 1000",
            "TRIANGULAR, 2, 1500",
            "TRIANGULAR, 3, 2000",
            "QUADRATIC, 1, 1500",
            "QUADRATIC, 2, 2500",
            "QUADRATIC, 3, 3500",
            "LINEAR, 1, 700",
            "LINEAR, 2, 900",
            "LINEAR, 3, 1100",
            "CUSTOM, 1, 1000"
    })
    void pointsToNextLevel_returnsCorrectValue(String formula, int level, long expected) throws Exception {
        setFormula(formula);
        if ("LINEAR".equalsIgnoreCase(formula)) {
            setIncrement(200);
        }
        assertThat(service.pointsToNextLevel(level)).isEqualTo(expected);
    }

    @Test
    void pointsToNextLevel_levelZeroOrNegative_returns1000() throws Exception {
        setFormula("TRIANGULAR");
        assertThat(service.pointsToNextLevel(0)).isEqualTo(1000L);
        assertThat(service.pointsToNextLevel(-5)).isEqualTo(1000L);
    }

    @Test
    void triangular_edgeCases() throws Exception {
        setFormula("TRIANGULAR");
        assertThat(service.calculateLevel(0)).isEqualTo(1);
        assertThat(service.calculateLevel(499)).isEqualTo(1);
        assertThat(service.calculateLevel(500)).isEqualTo(2);
        assertThat(service.calculateLevel(1499)).isEqualTo(2);
        assertThat(service.calculateLevel(1500)).isEqualTo(3);
        assertThat(service.calculateLevel(3000)).isEqualTo(4);
        assertThat(service.calculateLevel(5000)).isEqualTo(5);
    }

    @Test
    void quadratic_edgeCases() throws Exception {
        setFormula("QUADRATIC");
        assertThat(service.calculateLevel(0)).isEqualTo(1);
        assertThat(service.calculateLevel(499)).isEqualTo(1);
        assertThat(service.calculateLevel(500)).isEqualTo(2);
        assertThat(service.calculateLevel(1999)).isEqualTo(2);
        assertThat(service.calculateLevel(2000)).isEqualTo(3);
        assertThat(service.calculateLevel(4500)).isEqualTo(4);
    }

    @Test
    void linear_edgeCases() throws Exception {
        setFormula("LINEAR");
        setIncrement(200);
        assertThat(service.calculateLevel(0)).isEqualTo(1);
        assertThat(service.calculateLevel(499)).isEqualTo(1);
        assertThat(service.calculateLevel(500)).isEqualTo(2);
        assertThat(service.calculateLevel(1199)).isEqualTo(2);
        assertThat(service.calculateLevel(1200)).isEqualTo(3);
        assertThat(service.calculateLevel(2100)).isEqualTo(4);
    }

    @Test
    void linear_customBaseAndIncrement() throws Exception {
        setFormula("LINEAR");
        setBase(1000);
        setIncrement(300);
        assertThat(service.calculateLevel(0)).isEqualTo(1);
        assertThat(service.calculateLevel(999)).isEqualTo(1);
        assertThat(service.calculateLevel(1000)).isEqualTo(2);
        assertThat(service.calculateLevel(2299)).isEqualTo(2);
        assertThat(service.calculateLevel(2300)).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRIANGULAR", "QUADRATIC", "LINEAR", "UNKNOWN", "triangular", "quadratic", "linear", "unknown"})
    void differentFormulas_switchCorrectly(String formula) throws Exception {
        setFormula(formula);
        setBase(500);
        setIncrement(200);

        int level = service.calculateLevel(1500);
        long toNext = service.pointsToNextLevel(2);

        String upper = formula.toUpperCase();

        switch (upper) {
            case "TRIANGULAR" -> {
                assertThat(level).isEqualTo(3);
                assertThat(toNext).isEqualTo(1500L);
            }
            case "QUADRATIC" -> {
                assertThat(level).isEqualTo(2);
                assertThat(toNext).isEqualTo(2500L);
            }
            case "LINEAR" -> {
                assertThat(level).isEqualTo(3);
                assertThat(toNext).isEqualTo(900L);
            }
            default -> {
                assertThat(level).isEqualTo(2);
                assertThat(toNext).isEqualTo(1000L);
            }
        }
    }
}