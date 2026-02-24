package ru.misis.gamification.service.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.service.event.EventTypeService;
import ru.misis.gamification.service.point.result.AwardResult;
import ru.misis.gamification.service.point.result.AwardStatus;
import ru.misis.gamification.service.progress.LevelCalculatorService;
import ru.misis.gamification.service.transaction.TransactionService;
import ru.misis.gamification.service.user.UserService;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointsAwardingServiceTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private EventTypeService eventTypeService;

    @Mock
    private UserService userService;

    @Mock
    private LevelCalculatorService levelCalculatorService;

    @InjectMocks
    private PointsAwardingServiceImpl pointsAwardingService;

    @Captor
    private ArgumentCaptor<Transaction> txCaptor;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private LmsEventRequestDto request;
    private User user;
    private EventType eventType;

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

        eventType = EventType.builder()
                .typeCode("quiz")
                .displayName("Квиз / Тест")
                .points(100)
                .maxDailyPoints(500)
                .active(true)
                .build();
    }

    @Test
    void awardPoints_success_levelUp() {
        when(transactionService.isExistsByEventId("evt-456")).thenReturn(false);
        when(eventTypeService.getActiveByCode("quiz")).thenReturn(eventType);
        when(userService.createIfNotExists("user-123")).thenReturn(user);
        when(transactionService.sumPointsByUserIdAndEventTypeAndDate(
                eq("user-123"), eq("quiz"), any(LocalDate.class))).thenReturn(300L);
        when(transactionService.saveIfNotExists(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setUuid(UUID.randomUUID());
            return tx;
        });
        when(levelCalculatorService.calculateLevel(1300)).thenReturn(6);

        AwardResult result = pointsAwardingService.awardPoints(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPointsEarned()).isEqualTo(100);
        assertThat(result.getTotalPointsAfter()).isEqualTo(1300);
        assertThat(result.getNewLevel()).isEqualTo(6);
        assertThat(result.isLevelUp()).isTrue();
        assertThat(result.getTransactionId()).isNotNull();

        verify(transactionService).saveIfNotExists(txCaptor.capture());
        Transaction capturedTx = txCaptor.getValue();
        assertThat(capturedTx.getUserId()).isEqualTo("user-123");
        assertThat(capturedTx.getEventId()).isEqualTo("evt-456");
        assertThat(capturedTx.getEventTypeCode()).isEqualTo("quiz");
        assertThat(capturedTx.getPointsEarned()).isEqualTo(100);
        assertThat(capturedTx.getDescription()).contains("Квиз / Тест");

        verify(userService).update(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getTotalPoints()).isEqualTo(1300);
        assertThat(capturedUser.getLevel()).isEqualTo(6);
    }

    @Test
    void awardPoints_duplicateEvent() {
        when(transactionService.isExistsByEventId("evt-456")).thenReturn(true);

        AwardResult result = pointsAwardingService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.DUPLICATE);

        verify(transactionService).isExistsByEventId("evt-456");
        verifyNoInteractions(eventTypeService, userService, levelCalculatorService);
    }

    @Test
    void awardPoints_eventTypeNotFound() {
        when(transactionService.isExistsByEventId(anyString())).thenReturn(false);
        when(eventTypeService.getActiveByCode("quiz"))
                .thenThrow(new EventTypeNotFoundException("quiz"));

        AwardResult result = pointsAwardingService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.REJECTED);
        assertThat(result.getRejectionReason()).contains("Неизвестный или отключённый тип события");

        verify(eventTypeService).getActiveByCode("quiz");
    }

    @Test
    void awardPoints_dailyLimitExceeded() {
        when(transactionService.isExistsByEventId(anyString())).thenReturn(false);
        when(eventTypeService.getActiveByCode(anyString())).thenReturn(eventType);
        when(userService.createIfNotExists(anyString())).thenReturn(user);
        when(transactionService.sumPointsByUserIdAndEventTypeAndDate(
                anyString(), eq("quiz"), any())).thenReturn(450L);

        AwardResult result = pointsAwardingService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.REJECTED);
        assertThat(result.getRejectionReason()).contains("Превышен дневной лимит");

        verify(transactionService).sumPointsByUserIdAndEventTypeAndDate(anyString(), eq("quiz"), any());
    }
}