package ru.misis.gamification.service.application.awarding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.dto.lms.response.LmsEventResponseDto;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.model.AwardResultView;
import ru.misis.gamification.service.simple.eventtype.EventTypeService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LmsEventProcessorApplicationServiceUnitTest {

    @Mock
    private AwardingOrchestratorApplicationService awardingOrchestrator;

    @Mock
    private EventTypeService eventTypeService;

    @InjectMocks
    private LmsEventProcessorApplicationServiceImpl processor;

    private LmsEventRequestDto request;
    private EventType eventType;

    @BeforeEach
    void setUp() {
        request = LmsEventRequestDto.builder()
                .userId("user-12345")
                .eventId("evt-uuid-001")
                .eventType("quiz")
                .courseId("MATH-101")
                .groupId("G-1")
                .build();

        eventType = EventType.builder()
                .uuid(UUID.randomUUID())
                .typeCode("quiz")
                .displayName("Квиз / Тест")
                .points(80)
                .build();
    }

    @Test
    void process_success_returnsFullSuccessResponse() {
        AwardResultView successResult = new AwardResultView(
                true,
                80,
                1250,
                true,
                7,
                150L,
                83.33,
                null,
                false
        );

        when(awardingOrchestrator.awardPoints(
                eq("user-12345"), eq("evt-uuid-001"), eq("quiz"),
                eq("MATH-101"), eq("G-1")))
                .thenReturn(successResult);

        when(eventTypeService.getActiveByCode("quiz")).thenReturn(eventType);

        LmsEventResponseDto response = processor.process(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getUserId()).isEqualTo("user-12345");
        assertThat(response.getEventId()).isEqualTo("evt-uuid-001");
        assertThat(response.getDisplayName()).isEqualTo("Квиз / Тест");
        assertThat(response.getPointsEarned()).isEqualTo(80);
        assertThat(response.getTotalPoints()).isEqualTo(1250);
        assertThat(response.getLevelUp()).isTrue();
        assertThat(response.getNewLevel()).isEqualTo(7);
        assertThat(response.getPointsToNextLevel()).isEqualTo(150L);
        assertThat(response.getProgressPercent()).isEqualTo(83.33);
        assertThat(response.getProcessedAt()).isNotNull();

        verify(awardingOrchestrator).awardPoints(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(eventTypeService).getActiveByCode("quiz");
        verifyNoMoreInteractions(awardingOrchestrator, eventTypeService);
    }

    @Test
    void process_duplicate_returnsDuplicateResponse() {
        AwardResultView duplicateResult = new AwardResultView(
                false, 0, 0, false, 0, 0L, 0.0, null, true
        );

        when(awardingOrchestrator.awardPoints(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(duplicateResult);

        LmsEventResponseDto response = processor.process(request);

        assertThat(response.isDuplicate()).isTrue();
        assertThat(response.getStatus()).isEqualTo("duplicate");
        assertThat(response.getEventId()).isEqualTo("evt-uuid-001");
        assertThat(response.getMessage()).contains("Событие с ID evt-uuid-001 уже обработано ранее");
        assertThat(response.getProcessedAt()).isNotNull();

        verifyNoInteractions(eventTypeService);
    }

    @Test
    void process_rejected_returnsErrorResponse() {
        AwardResultView rejectedResult = new AwardResultView(
                false, 0, 0, false, 0, 0L, 0.0,
                "Превышен дневной лимит по типу Квиз", false
        );

        when(awardingOrchestrator.awardPoints(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(rejectedResult);

        LmsEventResponseDto response = processor.process(request);

        assertThat(response.isError()).isTrue();
        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getMessage()).isEqualTo("Превышен дневной лимит по типу Квиз");
        assertThat(response.getProcessedAt()).isNotNull();

        verifyNoInteractions(eventTypeService);
    }

    @Test
    void process_rejectedWithoutReason_returnsErrorWithDefaultMessage() {
        AwardResultView rejected = new AwardResultView(
                false, 0, 0, false, 0, 0L, 0.0, null, false
        );

        when(awardingOrchestrator.awardPoints(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(rejected);

        LmsEventResponseDto response = processor.process(request);

        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getMessage()).isEqualTo("Внутренняя ошибка обработки события");
    }

    @Test
    void process_success_callsEventTypeService() {
        AwardResultView success = new AwardResultView(
                true, 50, 1000, false, 5, 200L, 50.0, null, false
        );

        when(awardingOrchestrator.awardPoints(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(success);
        when(eventTypeService.getActiveByCode("quiz")).thenReturn(eventType);

        processor.process(request);

        verify(eventTypeService).getActiveByCode("quiz");
    }

    @Test
    void process_logsDebugMessage() {
        AwardResultView success = new AwardResultView(true, 10, 100, false, 1, 90L, 11.11, null, false);

        when(awardingOrchestrator.awardPoints(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(success);
        when(eventTypeService.getActiveByCode(anyString())).thenReturn(eventType);

        processor.process(request);

        verify(awardingOrchestrator).awardPoints(eq("user-12345"), eq("evt-uuid-001"), eq("quiz"), eq("MATH-101"), eq("G-1"));
    }
}