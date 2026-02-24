package ru.misis.gamification.service.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.service.point.result.AwardResult;
import ru.misis.gamification.service.point.result.AwardStatus;
import ru.misis.gamification.service.progress.calculator.ProgressCalculator;
import ru.misis.gamification.service.progress.result.ProgressMetrics;
import ru.misis.gamification.service.user.UserService;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointsServiceTest {

    @Mock
    private PointsAwardingServiceImpl awardingDomainService;

    @Mock
    private ProgressCalculator progressCalculator;

    @Mock
    private UserService userService;

    @InjectMocks
    private PointsServiceImpl pointsService;

    private LmsEventRequestDto request;
    private User user;
    private Transaction savedTx;

    @BeforeEach
    void setUp() {
        request = LmsEventRequestDto.builder()
                .userId("user-123")
                .eventId("evt-456")
                .eventType("quiz")
                .build();

        user = User.builder()
                .uuid(UUID.randomUUID())
                .userId("user-123")
                .totalPoints(1200)
                .level(5)
                .build();

        savedTx = Transaction.builder()
                .uuid(UUID.randomUUID())
                .userId("user-123")
                .eventId("evt-456")
                .eventTypeCode("quiz")
                .pointsEarned(100)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void awardPoints_success_levelUpTrue() {
        AwardResult domainResult = AwardResult.success(
                100, 1300, 6, true, savedTx.getUuid(), 800L, 62.5
        );

        ProgressMetrics metrics = new ProgressMetrics(800L, 62.5);

        when(awardingDomainService.awardPoints(request)).thenReturn(domainResult);
        when(userService.get("user-123")).thenReturn(user);
        when(progressCalculator.calculate(user)).thenReturn(metrics);

        AwardResult result = pointsService.awardPoints(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPointsEarned()).isEqualTo(100);
        assertThat(result.getTotalPointsAfter()).isEqualTo(1300);
        assertThat(result.getNewLevel()).isEqualTo(6);
        assertThat(result.isLevelUp()).isTrue();
        assertThat(result.getPointsToNextLevel()).isEqualTo(800L);
        assertThat(result.getProgressPercent()).isEqualTo(62.5);
        assertThat(result.getTransactionId()).isEqualTo(savedTx.getUuid());

        verify(awardingDomainService).awardPoints(request);
        verify(userService).get("user-123");
        verify(progressCalculator).calculate(user);
        verifyNoMoreInteractions(awardingDomainService, progressCalculator, userService);
    }

    @Test
    void awardPoints_duplicate_returnsDuplicate() {
        when(awardingDomainService.awardPoints(request)).thenReturn(AwardResult.duplicate());

        AwardResult result = pointsService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.DUPLICATE);

        verify(awardingDomainService).awardPoints(request);
        verifyNoInteractions(progressCalculator, userService);
    }

    @Test
    void awardPoints_rejected_returnsRejected() {
        when(awardingDomainService.awardPoints(request)).thenReturn(
                AwardResult.rejected("Превышен дневной лимит")
        );

        AwardResult result = pointsService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.REJECTED);
        assertThat(result.getRejectionReason()).isEqualTo("Превышен дневной лимит");

        verify(awardingDomainService).awardPoints(request);
        verifyNoInteractions(progressCalculator, userService);
    }

    @Test
    void awardPoints_eventTypeNotFoundFromDomain_returnsRejected() {
        when(awardingDomainService.awardPoints(request)).thenReturn(
                AwardResult.rejected("Неизвестный или отключённый тип события: quiz")
        );

        AwardResult result = pointsService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.REJECTED);
        assertThat(result.getRejectionReason()).contains("Неизвестный или отключённый тип события");

        verify(awardingDomainService).awardPoints(request);
        verifyNoInteractions(progressCalculator, userService);
    }
}