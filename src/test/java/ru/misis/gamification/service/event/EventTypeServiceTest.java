package ru.misis.gamification.service.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.repository.EventTypeRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventTypeServiceTest {

    @Mock
    private EventTypeRepository eventTypeRepository;

    @InjectMocks
    private EventTypeServiceImpl service;

    private EventType testType;

    @BeforeEach
    void setUp() {
        testType = EventType.builder()
                .typeCode("quiz")
                .displayName("Тест")
                .points(80)
                .maxDailyPoints(300)
                .active(true)
                .build();
    }

    @Test
    void getActiveByCode_existingActive_returnsType() {
        when(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz")).thenReturn(Optional.of(testType));

        EventType result = service.getActiveByCode("quiz");

        assertThat(result).isEqualTo(testType);
    }

    @Test
    void getActiveByCode_inactive_throwsEntityNotFound() {
        when(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getActiveByCode("quiz"))
                .isInstanceOf(EventTypeNotFoundException.class);
    }

    @Test
    void canAwardPoints_noLimit_returnsTrue() {
        when(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz")).thenReturn(Optional.of(testType));

        boolean canAward = service.canAwardPoints("user-123", "quiz", 100, LocalDate.now());

        assertThat(canAward).isTrue();
    }

    @Test
    void canAwardPoints_withinLimit_returnsTrue() {
        when(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz")).thenReturn(Optional.of(testType));
        when(eventTypeRepository.sumPointsByUserIdAndEventTypeAndDate("user-123", "quiz", LocalDate.now()))
                .thenReturn(150L);

        boolean canAward = service.canAwardPoints("user-123", "quiz", 100, LocalDate.now());

        assertThat(canAward).isTrue(); // 150 + 100 = 250 ≤ 300
    }

    @Test
    void canAwardPoints_exceedsLimit_returnsFalse() {
        when(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz")).thenReturn(Optional.of(testType));
        when(eventTypeRepository.sumPointsByUserIdAndEventTypeAndDate("user-123", "quiz", LocalDate.now()))
                .thenReturn(250L);

        boolean canAward = service.canAwardPoints("user-123", "quiz", 100, LocalDate.now());

        assertThat(canAward).isFalse(); // 250 + 100 = 350 > 300
    }

    @Test
    void getDailyPointsSum_returnsCorrectSum() {
        when(eventTypeRepository.sumPointsByUserIdAndEventTypeAndDate("user-123", "quiz", LocalDate.now()))
                .thenReturn(180L);

        long sum = service.getDailyPointsSum("user-123", "quiz", LocalDate.now());

        assertThat(sum).isEqualTo(180L);
    }

    @Test
    void getDailyPointsSum_noRecords_returnsZero() {
        when(eventTypeRepository.sumPointsByUserIdAndEventTypeAndDate("user-123", "quiz", LocalDate.now()))
                .thenReturn(0L);

        long sum = service.getDailyPointsSum("user-123", "quiz", LocalDate.now());

        assertThat(sum).isZero();
    }
}