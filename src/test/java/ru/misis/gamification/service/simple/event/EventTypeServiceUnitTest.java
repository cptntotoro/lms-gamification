package ru.misis.gamification.service.simple.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.repository.EventTypeRepository;
import ru.misis.gamification.service.simple.eventtype.EventTypeServiceImpl;
import ru.misis.gamification.service.simple.user.UserService;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventTypeServiceUnitTest {

    @Mock
    private EventTypeRepository eventTypeRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private EventTypeServiceImpl service;

    private EventType testType;
    private UUID userUuid;
    private UUID eventTypeUuid;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        testType = EventType.builder()
                .uuid(UUID.randomUUID())
                .typeCode("quiz")
                .displayName("Тест / Квиз")
                .points(80)
                .maxDailyPoints(300)
                .active(true)
                .build();

        userUuid = UUID.randomUUID();
        eventTypeUuid = testType.getUuid();
        today = LocalDate.now();
    }

    @Test
    void getActiveByCode_existingActive_returnsType() {
        when(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz"))
                .thenReturn(Optional.of(testType));

        EventType result = service.getActiveByCode("quiz");

        assertThat(result).isEqualTo(testType);
        verify(eventTypeRepository).findByTypeCodeAndActiveTrue("quiz");
    }

    @Test
    void getActiveByCode_inactiveOrNotFound_throwsEventTypeNotFoundException() {
        when(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getActiveByCode("quiz"))
                .isInstanceOf(EventTypeNotFoundException.class)
                .hasMessageContaining("quiz");

        verify(eventTypeRepository).findByTypeCodeAndActiveTrue("quiz");
    }

    @Test
    void canAwardPoints_noMaxLimit_returnsTrue() {
        testType.setMaxDailyPoints(null);
        when(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz"))
                .thenReturn(Optional.of(testType));

        boolean canAward = service.canAwardPoints("user-123", "quiz", 500, today);

        assertThat(canAward).isTrue();
        verify(eventTypeRepository).findByTypeCodeAndActiveTrue("quiz");
    }

    @Test
    void canAwardPoints_withinLimit_returnsTrue() {
        when(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz"))
                .thenReturn(Optional.of(testType));
        when(userService.getUserUuidByExternalId("user-123")).thenReturn(userUuid);
        when(eventTypeRepository.calculateDailyPointsSumForUserAndType(userUuid, eventTypeUuid, today))
                .thenReturn(150L);

        boolean canAward = service.canAwardPoints("user-123", "quiz", 100, today);

        assertThat(canAward).isTrue(); // 150 + 100 = 250 ≤ 300
        verify(eventTypeRepository).calculateDailyPointsSumForUserAndType(userUuid, eventTypeUuid, today);
    }

    @Test
    void canAwardPoints_exceedsLimit_returnsFalse() {
        when(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz"))
                .thenReturn(Optional.of(testType));
        when(userService.getUserUuidByExternalId("user-123")).thenReturn(userUuid);
        when(eventTypeRepository.calculateDailyPointsSumForUserAndType(userUuid, eventTypeUuid, today))
                .thenReturn(250L);

        boolean canAward = service.canAwardPoints("user-123", "quiz", 100, today);

        assertThat(canAward).isFalse(); // 250 + 100 = 350 > 300
    }

    @Test
    void canAwardPoints_typeNotFound_throwsEventTypeNotFoundException() {
        when(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.canAwardPoints("user-123", "quiz", 100, today))
                .isInstanceOf(EventTypeNotFoundException.class)
                .hasMessageContaining("quiz");
    }

    @Test
    void getDailyPointsSum_returnsCorrectSum() {
        when(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz"))
                .thenReturn(Optional.of(testType));
        when(userService.getUserUuidByExternalId("user-123")).thenReturn(userUuid);
        when(eventTypeRepository.calculateDailyPointsSumForUserAndType(userUuid, eventTypeUuid, today))
                .thenReturn(180L);

        long sum = service.getDailyPointsSum("user-123", "quiz", today);

        assertThat(sum).isEqualTo(180L);
        verify(eventTypeRepository).calculateDailyPointsSumForUserAndType(userUuid, eventTypeUuid, today);
    }

    @Test
    void getDailyPointsSum_noRecords_returnsZero() {
        when(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz"))
                .thenReturn(Optional.of(testType));
        when(userService.getUserUuidByExternalId("user-123")).thenReturn(userUuid);
        when(eventTypeRepository.calculateDailyPointsSumForUserAndType(userUuid, eventTypeUuid, today))
                .thenReturn(0L);

        long sum = service.getDailyPointsSum("user-123", "quiz", today);

        assertThat(sum).isZero();
    }

    @Test
    void getDailyPointsSum_typeNotFound_throwsEventTypeNotFoundException() {
        when(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDailyPointsSum("user-123", "quiz", today))
                .isInstanceOf(EventTypeNotFoundException.class)
                .hasMessageContaining("quiz");
    }
}