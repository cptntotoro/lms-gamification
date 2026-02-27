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
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.entity.Transaction;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.service.course.UserCourseServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

    @Mock
    private UserCourseServiceImpl userCourseService;

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
                .courseId("MATH-101")
                .groupId("1-A")
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
        when(userService.createIfNotExists("user-123", "MATH-101", "1-A")).thenReturn(user);
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

        verify(userCourseService).addPointsToCourse(user, "MATH-101", 100);
    }

    @Test
    void awardPoints_duplicateEvent() {
        when(transactionService.isExistsByEventId("evt-456")).thenReturn(true);

        AwardResult result = pointsAwardingService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.DUPLICATE);

        verify(transactionService).isExistsByEventId("evt-456");
        verifyNoInteractions(eventTypeService, userService, levelCalculatorService, userCourseService);
    }

    @Test
    void awardPoints_eventTypeNotFound() {
        when(transactionService.isExistsByEventId(any())).thenReturn(false);
        when(eventTypeService.getActiveByCode("quiz"))
                .thenThrow(new EventTypeNotFoundException("quiz"));

        AwardResult result = pointsAwardingService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.REJECTED);
        assertThat(result.getRejectionReason()).contains("Неизвестный или отключённый тип события");

        verify(eventTypeService).getActiveByCode("quiz");
        verifyNoInteractions(userService, levelCalculatorService, userCourseService);
    }

    @Test
    void awardPoints_dailyLimitExceeded() {
        when(transactionService.isExistsByEventId(any())).thenReturn(false);
        when(eventTypeService.getActiveByCode(any())).thenReturn(eventType);
        when(userService.createIfNotExists(any(), any(), any())).thenReturn(user);
        when(transactionService.sumPointsByUserIdAndEventTypeAndDate(
                any(), eq("quiz"), any())).thenReturn(450L);

        AwardResult result = pointsAwardingService.awardPoints(request);

        assertThat(result.getStatus()).isEqualTo(AwardStatus.REJECTED);
        assertThat(result.getRejectionReason()).contains("Превышен дневной лимит");

        verify(transactionService).sumPointsByUserIdAndEventTypeAndDate(any(), eq("quiz"), any());
        verifyNoInteractions(userCourseService);
    }

    @Test
    void awardPoints_noCourseId_doesNotCallCourseService() {
        request.setCourseId(null);

        when(transactionService.isExistsByEventId(any())).thenReturn(false);
        when(eventTypeService.getActiveByCode(any())).thenReturn(eventType);
        when(userService.createIfNotExists(any(), eq(null), any())).thenReturn(user);
        when(transactionService.sumPointsByUserIdAndEventTypeAndDate(any(), any(), any())).thenReturn(0L);
        when(transactionService.saveIfNotExists(any())).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setUuid(UUID.randomUUID());
            return tx;
        });
        when(levelCalculatorService.calculateLevel(anyInt())).thenReturn(5);

        pointsAwardingService.awardPoints(request);

        verify(userCourseService, never()).addPointsToCourse(any(), any(), anyInt());
    }
}