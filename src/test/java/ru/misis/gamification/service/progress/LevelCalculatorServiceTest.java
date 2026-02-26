package ru.misis.gamification.service.progress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LevelCalculatorServiceTest {

    @InjectMocks
    private LevelCalculatorServiceImpl service;

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
            "124750,  22",
            "127500,  23"
    })
    void calculateLevel_triangular_returnsCorrectLevel(int totalPoints, int expectedLevel) {
        ReflectionTestUtils.setField(service, "formula", "TRIANGULAR");
        ReflectionTestUtils.setField(service, "base", 500);
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
            "1247500,  50",
            "1322500,  52"
    })
    void calculateLevel_quadratic_returnsCorrectLevel(int totalPoints, int expectedLevel) {
        ReflectionTestUtils.setField(service, "formula", "QUADRATIC");
        ReflectionTestUtils.setField(service, "base", 500);
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
            "124750,  34",
            "127500,  34"
    })
    void calculateLevel_linear_returnsCorrectLevel(int totalPoints, int expectedLevel) {
        ReflectionTestUtils.setField(service, "formula", "LINEAR");
        ReflectionTestUtils.setField(service, "base", 500);
        ReflectionTestUtils.setField(service, "increment", 200);
        assertThat(service.calculateLevel(totalPoints)).isEqualTo(expectedLevel);
    }

    @Test
    void calculateLevel_triangular_zeroOrNegative_returns1() {
        ReflectionTestUtils.setField(service, "formula", "TRIANGULAR");
        ReflectionTestUtils.setField(service, "base", 500);
        assertThat(service.calculateLevel(0)).isEqualTo(1);
        assertThat(service.calculateLevel(-1)).isEqualTo(1);
        assertThat(service.calculateLevel(-500)).isEqualTo(1);
    }

    @Test
    void calculateLevel_quadratic_zeroOrNegative_returns1() {
        ReflectionTestUtils.setField(service, "formula", "QUADRATIC");
        ReflectionTestUtils.setField(service, "base", 500);
        assertThat(service.calculateLevel(0)).isEqualTo(1);
        assertThat(service.calculateLevel(-1)).isEqualTo(1);
        assertThat(service.calculateLevel(-500)).isEqualTo(1);
    }

    @Test
    void calculateLevel_linear_zeroOrNegative_returns1() {
        ReflectionTestUtils.setField(service, "formula", "LINEAR");
        ReflectionTestUtils.setField(service, "base", 500);
        ReflectionTestUtils.setField(service, "increment", 200);
        assertThat(service.calculateLevel(0)).isEqualTo(1);
        assertThat(service.calculateLevel(-1)).isEqualTo(1);
        assertThat(service.calculateLevel(-500)).isEqualTo(1);
    }

    @Test
    void pointsToNextLevel_triangular() {
        ReflectionTestUtils.setField(service, "formula", "TRIANGULAR");
        ReflectionTestUtils.setField(service, "base", 500);

        assertThat(service.pointsToNextLevel(1)).isEqualTo(1000L);
        assertThat(service.pointsToNextLevel(2)).isEqualTo(1500L);
        assertThat(service.pointsToNextLevel(3)).isEqualTo(2000L);
        assertThat(service.pointsToNextLevel(50)).isEqualTo(25500L);
    }

    @Test
    void pointsToNextLevel_quadratic() {
        ReflectionTestUtils.setField(service, "formula", "QUADRATIC");
        ReflectionTestUtils.setField(service, "base", 500);

        assertThat(service.pointsToNextLevel(1)).isEqualTo(1500L);
        assertThat(service.pointsToNextLevel(2)).isEqualTo(2500L);
        assertThat(service.pointsToNextLevel(3)).isEqualTo(3500L);
        assertThat(service.pointsToNextLevel(50)).isEqualTo(50500L);
    }

    @Test
    void pointsToNextLevel_linear() {
        ReflectionTestUtils.setField(service, "formula", "LINEAR");
        ReflectionTestUtils.setField(service, "base", 500);
        ReflectionTestUtils.setField(service, "increment", 200);

        assertThat(service.pointsToNextLevel(1)).isEqualTo(700L);
        assertThat(service.pointsToNextLevel(2)).isEqualTo(900L);
        assertThat(service.pointsToNextLevel(3)).isEqualTo(1100L);
        assertThat(service.pointsToNextLevel(50)).isEqualTo(10500L);
    }

    @Test
    void calculateLevel_negativePoints_returnsLevel1() {
        ReflectionTestUtils.setField(service, "formula", "TRIANGULAR");
        ReflectionTestUtils.setField(service, "base", 500);
        assertThat(service.calculateLevel(-100)).isEqualTo(1);
    }

    @Test
    void calculateLevel_unknownFormula_usesFallback() {
        ReflectionTestUtils.setField(service, "formula", "UNKNOWN");
        ReflectionTestUtils.setField(service, "base", 500);
        assertThat(service.calculateLevel(0)).isEqualTo(1);
        assertThat(service.calculateLevel(999)).isEqualTo(1);
        assertThat(service.calculateLevel(1000)).isEqualTo(2);
        assertThat(service.calculateLevel(5000)).isEqualTo(6);
        assertThat(service.calculateLevel(999999)).isEqualTo(1000);
    }

    @Test
    void calculateLevel_triangular_largeValue_noOverflow() {
        ReflectionTestUtils.setField(service, "formula", "TRIANGULAR");
        ReflectionTestUtils.setField(service, "base", 500);
        assertThat(service.calculateLevel(Integer.MAX_VALUE)).isGreaterThan(0);
    }

    @Test
    void calculateLevel_quadratic_largeValue_noOverflow() {
        ReflectionTestUtils.setField(service, "formula", "QUADRATIC");
        ReflectionTestUtils.setField(service, "base", 500);
        assertThat(service.calculateLevel(Integer.MAX_VALUE)).isGreaterThan(0);
    }

    @Test
    void pointsToNextLevel_triangular_largeLevel() {
        ReflectionTestUtils.setField(service, "formula", "TRIANGULAR");
        ReflectionTestUtils.setField(service, "base", 500);
        assertThat(service.pointsToNextLevel(1000)).isEqualTo(500500L);
    }

    @Test
    void pointsToNextLevel_quadratic_largeLevel() {
        ReflectionTestUtils.setField(service, "formula", "QUADRATIC");
        ReflectionTestUtils.setField(service, "base", 500);
        assertThat(service.pointsToNextLevel(1000)).isEqualTo(1000500L);
    }

    @Test
    void pointsToNextLevel_linear_largeLevel() {
        ReflectionTestUtils.setField(service, "formula", "LINEAR");
        ReflectionTestUtils.setField(service, "base", 500);
        ReflectionTestUtils.setField(service, "increment", 200);
        assertThat(service.pointsToNextLevel(1000)).isEqualTo(200500L);
    }

    @Test
    void configurationProperties_areApplied() {
        ReflectionTestUtils.setField(service, "formula", "LINEAR");
        ReflectionTestUtils.setField(service, "base", 100);
        ReflectionTestUtils.setField(service, "increment", 50);

        assertThat(service.calculateLevel(0)).isEqualTo(1);
        assertThat(service.calculateLevel(99)).isEqualTo(1);
        assertThat(service.calculateLevel(100)).isEqualTo(2);
        assertThat(service.pointsToNextLevel(1)).isEqualTo(150L);
    }
}