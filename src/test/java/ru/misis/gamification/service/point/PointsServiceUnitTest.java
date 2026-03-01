package ru.misis.gamification.service.point;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.service.point.result.AwardResult;
import ru.misis.gamification.service.point.result.AwardStatus;
import ru.misis.gamification.service.progress.calculator.ProgressCalculator;
import ru.misis.gamification.service.progress.result.ProgressMetrics;
import ru.misis.gamification.service.user.UserService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointsServiceUnitTest {

    @Mock
    private PointsAwardingService awardingService;
    @Mock
    private ProgressCalculator progressCalculator;
    @Mock
    private UserService userService;

    @InjectMocks
    private PointsServiceImpl pointsService;

    private LmsEventRequestDto createRequest() {
        return LmsEventRequestDto.builder()
                .userId("user-123")
                .eventId("evt-456")
                .eventType("quiz")
                .build();
    }

    @Test
    void awardPoints_success_levelUpTrue_enrichesWithProgressMetrics() {
        LmsEventRequestDto request = createRequest();

        AwardResult domainResult = AwardResult.success(
                100, 1300, 6, true, UUID.randomUUID(), 800L, 62.5
        );

        User user = User.builder()
                .uuid(UUID.randomUUID())
                .userId("user-123")
                .build();

        ProgressMetrics metrics = new ProgressMetrics(800L, 62.5);

        when(awardingService.awardPoints(request)).thenReturn(domainResult);
        when(userService.getUserUuidByExternalId("user-123")).thenReturn(user.getUuid());
        when(userService.getByUuid(user.getUuid())).thenReturn(user);
        when(progressCalculator.calculate(user)).thenReturn(metrics);

        AwardResult result = pointsService.awardPoints(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPointsEarned()).isEqualTo(100);
        assertThat(result.getTotalPointsAfter()).isEqualTo(1300);
        assertThat(result.getNewLevel()).isEqualTo(6);
        assertThat(result.isLevelUp()).isTrue();
        assertThat(result.getPointsToNextLevel()).isEqualTo(800L);
        assertThat(result.getProgressPercent()).isEqualTo(62.5);
        assertThat(result.getTransactionId()).isNotNull();

        verify(awardingService).awardPoints(request);
        verify(userService).getUserUuidByExternalId("user-123");
        verify(userService).getByUuid(user.getUuid());
        verify(progressCalculator).calculate(user);
        verifyNoMoreInteractions(awardingService, progressCalculator, userService);
    }

    @Test
    void awardPoints_duplicate_returnsDuplicateWithoutProgress() {
        LmsEventRequestDto request = createRequest();

        when(awardingService.awardPoints(request)).thenReturn(AwardResult.duplicate());

        AwardResult result = pointsService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.DUPLICATE);

        verify(awardingService).awardPoints(request);
        verifyNoInteractions(progressCalculator, userService);
    }

    @Test
    void awardPoints_rejected_returnsRejectedWithoutProgress() {
        LmsEventRequestDto request = createRequest();

        when(awardingService.awardPoints(request)).thenReturn(
                AwardResult.rejected("Превышен дневной лимит")
        );

        AwardResult result = pointsService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.REJECTED);
        assertThat(result.getRejectionReason()).isEqualTo("Превышен дневной лимит");

        verify(awardingService).awardPoints(request);
        verifyNoInteractions(progressCalculator, userService);
    }

    @Test
    void awardPoints_eventTypeNotFound_returnsRejectedWithoutProgress() {
        LmsEventRequestDto request = createRequest();

        when(awardingService.awardPoints(request)).thenReturn(
                AwardResult.rejected("Неизвестный или отключённый тип события: quiz")
        );

        AwardResult result = pointsService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.REJECTED);
        assertThat(result.getRejectionReason()).contains("Неизвестный или отключённый тип события");

        verify(awardingService).awardPoints(request);
        verifyNoInteractions(progressCalculator, userService);
    }
}