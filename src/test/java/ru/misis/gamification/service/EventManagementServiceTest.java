package ru.misis.gamification.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.dto.lms.response.LmsEventResponsetDto;
import ru.misis.gamification.dto.result.AwardResult;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.service.event.EventManagementServiceImpl;
import ru.misis.gamification.service.event.EventTypeService;
import ru.misis.gamification.service.point.PointsService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventManagementServiceTest {

    @Mock
    private PointsService pointsService;

    @Mock
    private EventTypeService eventTypeService;

    @InjectMocks
    private EventManagementServiceImpl eventManagementService;

    private final String userId = "user-123";
    private final String eventId = "evt-999";
    private final String typeCode = "quiz";
    private final UUID transactionId = UUID.randomUUID();

    @Test
    void shouldReturnSuccess_whenAwardResultIsSuccess() {
        LmsEventRequestDto request = createRequest();

        EventType eventType = EventType.builder()
                .typeCode(typeCode)
                .displayName("Квиз / Тест")
                .build();

        AwardResult awardResult = AwardResult.success(
                80, 1250, 12, true, transactionId, 750L
        );

        when(pointsService.awardPoints(request)).thenReturn(awardResult);
        when(eventTypeService.getActiveByCode(typeCode)).thenReturn(eventType);

        LmsEventResponsetDto response = eventManagementService.process(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getEventId()).isEqualTo(eventId);
        assertThat(response.getDisplayName()).isEqualTo("Квиз / Тест");
        assertThat(response.getPointsEarned()).isEqualTo(80);
        assertThat(response.getTotalPoints()).isEqualTo(1250);
        assertThat(response.getPointsToNextLevel()).isEqualTo(750L);
        assertThat(response.getTransactionId()).isEqualTo(transactionId);
        assertThat(response.getLevelUp()).isTrue();
        assertThat(response.getProcessedAt()).isNotNull();

        verify(pointsService).awardPoints(request);
        verify(eventTypeService).getActiveByCode(typeCode);
        verifyNoMoreInteractions(pointsService, eventTypeService);
    }

    @Test
    void shouldReturnDuplicate_whenAwardResultIsDuplicate() {
        LmsEventRequestDto request = createRequest();

        when(pointsService.awardPoints(request)).thenReturn(AwardResult.duplicate());

        LmsEventResponsetDto response = eventManagementService.process(request);

        assertThat(response.getStatus()).isEqualTo("duplicate");
        assertThat(response.getEventId()).isEqualTo(eventId);
        assertThat(response.getMessage()).contains(eventId);

        verify(pointsService).awardPoints(request);
        verifyNoInteractions(eventTypeService);
    }

    @Test
    void shouldReturnError_whenAwardResultIsRejected() {
        LmsEventRequestDto request = createRequest();

        when(pointsService.awardPoints(request)).thenReturn(
                AwardResult.rejected("Превышен дневной лимит")
        );

        LmsEventResponsetDto response = eventManagementService.process(request);

        assertThat(response.getStatus()).isEqualTo("error");
        assertThat(response.getMessage()).isEqualTo("Превышен дневной лимит");

        verify(pointsService).awardPoints(request);
        verifyNoInteractions(eventTypeService);
    }

    @Test
    void shouldReturnError_whenEventTypeNotFound() {
        LmsEventRequestDto request = createRequest();

        when(pointsService.awardPoints(request))
                .thenThrow(new EventTypeNotFoundException(typeCode));

        assertThatThrownBy(() -> eventManagementService.process(request))
                .isInstanceOf(EventTypeNotFoundException.class)
                .hasMessageContaining(typeCode);

        verify(pointsService).awardPoints(request);
        verifyNoInteractions(eventTypeService);
    }

    @Test
    void shouldReturnError_whenUnexpectedException() {
        LmsEventRequestDto request = createRequest();

        RuntimeException dbError = new RuntimeException("DB error");
        when(pointsService.awardPoints(request)).thenThrow(dbError);

        assertThatThrownBy(() -> eventManagementService.process(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");

        verify(pointsService).awardPoints(request);
        verifyNoInteractions(eventTypeService);
    }

    private LmsEventRequestDto createRequest() {
        return LmsEventRequestDto.builder()
                .userId(userId)
                .eventId(eventId)
                .eventType(typeCode)
                .build();
    }
}