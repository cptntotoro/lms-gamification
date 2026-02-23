package ru.misis.gamification.service.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.dto.AwardResult;
import ru.misis.gamification.dto.AwardStatus;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.service.event.EventTypeService;
import ru.misis.gamification.service.level.LevelCalculatorService;
import ru.misis.gamification.service.transaction.TransactionService;
import ru.misis.gamification.service.user.UserService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointsServiceTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserService userService;

    @Mock
    private LevelCalculatorService levelCalculatorService;

    @Mock
    private EventTypeService eventTypeService;

    @InjectMocks
    private PointsServiceImpl pointsService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<Transaction> txCaptor;

    private LmsEventRequestDto request;
    private User user;
    private EventType eventType;
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

        eventType = EventType.builder()
                .typeCode("quiz")
                .displayName("Квиз / Тест")
                .points(100)
                .maxDailyPoints(500)
                .build();

        savedTx = Transaction.builder()
                .uuid(UUID.randomUUID())
                .userId("user-123")
                .eventId("evt-456")
                .eventTypeCode("quiz")
                .pointsEarned(100)
                .build();
    }

    @Test
    void awardPoints_success_levelUpTrue() {
        when(userService.createIfNotExists("user-123")).thenReturn(user);
        when(eventTypeService.getActiveByCode("quiz")).thenReturn(eventType);
        when(transactionService.isExistsByEventId("evt-456")).thenReturn(false);
        when(transactionService.sumPointsByUserIdAndEventTypeAndDate(anyString(), eq("quiz"), any())).thenReturn(300L);
        when(transactionService.saveIfNotExists(any())).thenReturn(savedTx);
        when(levelCalculatorService.calculateLevel(1300)).thenReturn(6);
        when(levelCalculatorService.pointsToNextLevel(6)).thenReturn(800L);

        AwardResult result = pointsService.awardPoints(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPointsEarned()).isEqualTo(100);
        assertThat(result.getTotalPointsAfter()).isEqualTo(1300);
        assertThat(result.getLevelAfter()).isEqualTo(6);
        assertThat(result.isLevelUp()).isTrue();
        assertThat(result.getPointsToNextLevel()).isEqualTo(800L);
        assertThat(result.getTransactionId()).isEqualTo(savedTx.getUuid());

        // Проверяем, что пользователь обновлён
        verify(userService).update(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getTotalPoints()).isEqualTo(1300);
        assertThat(capturedUser.getLevel()).isEqualTo(6);
    }

    @Test
    void awardPoints_success_noLevelUp() {
        user.setLevel(5);
        when(userService.createIfNotExists(anyString())).thenReturn(user);
        when(eventTypeService.getActiveByCode(anyString())).thenReturn(eventType);
        when(transactionService.isExistsByEventId(anyString())).thenReturn(false);
        when(transactionService.sumPointsByUserIdAndEventTypeAndDate(anyString(), anyString(), any())).thenReturn(0L);
        when(transactionService.saveIfNotExists(any())).thenReturn(savedTx);
        when(levelCalculatorService.calculateLevel(anyInt())).thenReturn(5);
        when(levelCalculatorService.pointsToNextLevel(5)).thenReturn(800L);

        AwardResult result = pointsService.awardPoints(request);

        assertThat(result.isLevelUp()).isFalse();
    }

    @Test
    void awardPoints_duplicateEvent_returnsDuplicate() {
        when(transactionService.isExistsByEventId("evt-456")).thenReturn(true);

        AwardResult result = pointsService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.DUPLICATE);

        verify(transactionService).isExistsByEventId("evt-456");
        verifyNoInteractions(eventTypeService, userService, levelCalculatorService);
        verify(transactionService, never()).saveIfNotExists(any());
    }

    @Test
    void awardPoints_eventTypeNotFound_returnsRejected() {
        when(transactionService.isExistsByEventId(anyString())).thenReturn(false);
        when(eventTypeService.getActiveByCode("quiz"))
                .thenThrow(new EventTypeNotFoundException("quiz"));

        AwardResult result = pointsService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.REJECTED);
        assertThat(result.getRejectionReason()).contains("Неизвестный или отключённый тип события");
    }

    @Test
    void awardPoints_dailyLimitExceeded_returnsRejected() {
        when(transactionService.isExistsByEventId(anyString())).thenReturn(false);
        when(eventTypeService.getActiveByCode(anyString())).thenReturn(eventType);
        when(userService.createIfNotExists(anyString())).thenReturn(user);
        when(transactionService.sumPointsByUserIdAndEventTypeAndDate(anyString(), eq("quiz"), any()))
                .thenReturn(450L);

        AwardResult result = pointsService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.REJECTED);
        assertThat(result.getRejectionReason()).contains("Превышен дневной лимит");
    }

    @Test
    void awardPoints_success_verifiesTransactionArguments() {
        when(userService.createIfNotExists(anyString())).thenReturn(user);
        when(eventTypeService.getActiveByCode(anyString())).thenReturn(eventType);
        when(transactionService.isExistsByEventId(anyString())).thenReturn(false);
        when(transactionService.sumPointsByUserIdAndEventTypeAndDate(anyString(), anyString(), any())).thenReturn(0L);
        when(transactionService.saveIfNotExists(any())).thenReturn(savedTx);
        when(levelCalculatorService.calculateLevel(anyInt())).thenReturn(5);

        pointsService.awardPoints(request);

        verify(transactionService).saveIfNotExists(txCaptor.capture());
        Transaction capturedTx = txCaptor.getValue();

        assertThat(capturedTx.getUserId()).isEqualTo("user-123");
        assertThat(capturedTx.getEventId()).isEqualTo("evt-456");
        assertThat(capturedTx.getEventTypeCode()).isEqualTo("quiz");
        assertThat(capturedTx.getPointsEarned()).isEqualTo(100);
        assertThat(capturedTx.getDescription()).contains("Квиз / Тест");
    }

    @Test
    void awardPoints_success_verifiesUserUpdate() {
        when(userService.createIfNotExists(anyString())).thenReturn(user);
        when(eventTypeService.getActiveByCode(anyString())).thenReturn(eventType);
        when(transactionService.isExistsByEventId(anyString())).thenReturn(false);
        when(transactionService.sumPointsByUserIdAndEventTypeAndDate(anyString(), anyString(), any())).thenReturn(0L);
        when(transactionService.saveIfNotExists(any())).thenReturn(savedTx);
        when(levelCalculatorService.calculateLevel(1300)).thenReturn(6);

        pointsService.awardPoints(request);

        verify(userService).update(userCaptor.capture());
        User captured = userCaptor.getValue();

        assertThat(captured.getTotalPoints()).isEqualTo(1300);
        assertThat(captured.getLevel()).isEqualTo(6);
    }
}