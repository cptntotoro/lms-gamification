package ru.misis.gamification.service.event.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.dto.lms.response.LmsEventResponseDto;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.service.event.EventTypeService;
import ru.misis.gamification.service.point.PointsService;
import ru.misis.gamification.service.point.result.AwardResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LmsEventProcessorUnitTest {

    @Mock
    private PointsService pointsService;

    @Mock
    private EventTypeService eventTypeService;

    @InjectMocks
    private LmsEventProcessorImpl processor;

    private final String userId = "user-123";
    private final String eventId = "evt-999";
    private final String typeCode = "quiz";
    private final UUID transactionId = UUID.randomUUID();

    private LmsEventRequestDto createRequest() {
        return LmsEventRequestDto.builder()
                .userId(userId)
                .eventId(eventId)
                .eventType(typeCode)
                .build();
    }

    @Test
    void process_successAward_returnsSuccessResponseWithEventTypeName() {
        LmsEventRequestDto request = createRequest();

        AwardResult successResult = AwardResult.success(
                80, 1250, 12, true, transactionId, 750L, 62.5
        );

        EventType eventType = EventType.builder()
                .typeCode(typeCode)
                .displayName("Квиз / Тест")
                .build();

        when(pointsService.awardPoints(request)).thenReturn(successResult);
        when(eventTypeService.getActiveByCode(typeCode)).thenReturn(eventType);

        LmsEventResponseDto response = processor.process(request);

        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getEventId()).isEqualTo(eventId);
        assertThat(response.getDisplayName()).isEqualTo("Квиз / Тест");
        assertThat(response.getPointsEarned()).isEqualTo(80);
        assertThat(response.getTotalPoints()).isEqualTo(1250);
        assertThat(response.getLevelUp()).isTrue();
        assertThat(response.getPointsToNextLevel()).isEqualTo(750L);
        assertThat(response.getProgressPercent()).isEqualTo(62.5);
        assertThat(response.getTransactionId()).isEqualTo(transactionId);
        assertThat(response.getProcessedAt()).isNotNull();

        verify(pointsService).awardPoints(request);
        verify(eventTypeService).getActiveByCode(typeCode);
        verifyNoMoreInteractions(pointsService, eventTypeService);
    }

    @Test
    void process_duplicateAward_returnsDuplicateResponse() {
        LmsEventRequestDto request = createRequest();

        when(pointsService.awardPoints(request)).thenReturn(AwardResult.duplicate());

        LmsEventResponseDto response = processor.process(request);

        assertThat(response.getStatus()).isEqualTo("duplicate");
        assertThat(response.getEventId()).isEqualTo(eventId);
        assertThat(response.getMessage()).contains(eventId);

        verify(pointsService).awardPoints(request);
        verifyNoInteractions(eventTypeService);
    }

    @Test
    void process_rejectedAward_returnsErrorResponse() {
        LmsEventRequestDto request = createRequest();

        when(pointsService.awardPoints(request)).thenReturn(
                AwardResult.rejected("Превышен дневной лимит")
        );

        LmsEventResponseDto response = processor.process(request);

        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getMessage()).isEqualTo("Превышен дневной лимит");

        verify(pointsService).awardPoints(request);
        verifyNoInteractions(eventTypeService);
    }

    @Test
    void process_successButEventTypeNotFound_throwsEventTypeNotFoundException() {
        LmsEventRequestDto request = createRequest();

        AwardResult successResult = AwardResult.success(
                80, 1250, 12, true, transactionId, 750L, 62.5
        );

        when(pointsService.awardPoints(request)).thenReturn(successResult);
        when(eventTypeService.getActiveByCode(typeCode))
                .thenThrow(new EventTypeNotFoundException(typeCode));

        assertThatThrownBy(() -> processor.process(request))
                .isInstanceOf(EventTypeNotFoundException.class)
                .hasMessageContaining(typeCode);

        verify(pointsService).awardPoints(request);
        verify(eventTypeService).getActiveByCode(typeCode);
    }

    @Test
    void process_unexpectedExceptionFromPointsService_propagatesException() {
        LmsEventRequestDto request = createRequest();

        RuntimeException dbError = new RuntimeException("Database connection failed");
        when(pointsService.awardPoints(request)).thenThrow(dbError);

        assertThatThrownBy(() -> processor.process(request))
                .isSameAs(dbError);

        verify(pointsService).awardPoints(request);
        verifyNoInteractions(eventTypeService);
    }
}