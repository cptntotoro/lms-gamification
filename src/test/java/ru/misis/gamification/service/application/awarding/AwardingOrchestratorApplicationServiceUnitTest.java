package ru.misis.gamification.service.application.awarding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.entity.Transaction;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.model.AwardResultView;
import ru.misis.gamification.service.application.enrollment.EnrollmentApplicationService;
import ru.misis.gamification.service.simple.course.CourseService;
import ru.misis.gamification.service.simple.eventtype.EventTypeService;
import ru.misis.gamification.service.simple.progress.LevelCalculatorService;
import ru.misis.gamification.service.simple.transaction.TransactionService;
import ru.misis.gamification.service.simple.user.UserService;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AwardingOrchestratorApplicationServiceUnitTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private EventTypeService eventTypeService;

    @Mock
    private UserService userService;

    @Mock
    private LevelCalculatorService levelCalculator;

    @Mock
    private CourseService courseService;

    @Mock
    private EnrollmentApplicationService enrollmentApplicationService;

    @InjectMocks
    private AwardingOrchestratorApplicationServiceImpl service;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User user;
    private EventType eventType;
    private Course course;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .uuid(UUID.randomUUID())
                .userId("user-123")
                .totalPoints(500)
                .level(3)
                .build();

        eventType = EventType.builder()
                .uuid(UUID.randomUUID())
                .typeCode("quiz")
                .displayName("Квиз")
                .points(80)
                .maxDailyPoints(300)
                .active(true)
                .build();

        course = Course.builder()
                .uuid(UUID.randomUUID())
                .courseId("MATH-101")
                .build();
    }

    @Test
    void awardPoints_noUserId_returnsRejected() {
        AwardResultView result = service.awardPoints(null, "evt-001", "quiz", "MATH-101", "G-1");

        assertThat(result.success()).isFalse();
        assertThat(result.duplicate()).isFalse();
        assertThat(result.rejectionReason()).isEqualTo("Отсутствует идентификатор пользователя");

        verifyNoInteractions(transactionService, eventTypeService, userService, levelCalculator, courseService, enrollmentApplicationService);
    }

    @Test
    void awardPoints_duplicateEventId_returnsDuplicate() {
        when(transactionService.isExistsByEventId("evt-001")).thenReturn(true);

        AwardResultView result = service.awardPoints("user-123", "evt-001", "quiz", "MATH-101", "G-1");

        assertThat(result.success()).isFalse();
        assertThat(result.duplicate()).isTrue();
        assertThat(result.rejectionReason()).isNull();

        verify(transactionService).isExistsByEventId("evt-001");
        verifyNoMoreInteractions(transactionService);
    }

    @Test
    void awardPoints_eventTypeNotFound_returnsRejected() {
        when(eventTypeService.getActiveByCode("quiz"))
                .thenThrow(new RuntimeException("Тип не найден"));

        AwardResultView result = service.awardPoints("user-123", "evt-001", "quiz", null, null);

        assertThat(result.success()).isFalse();
        assertThat(result.duplicate()).isFalse();
        assertThat(result.rejectionReason()).contains("Неизвестный или отключённый тип события: quiz");

        verify(eventTypeService).getActiveByCode("quiz");
    }

    @Test
    void awardPoints_dailyLimitExceeded_returnsRejected() {
        when(eventTypeService.getActiveByCode("quiz")).thenReturn(eventType);
        when(userService.getUserByExternalId("user-123")).thenReturn(user);
        when(transactionService.isExistsByEventId(any())).thenReturn(false);
        when(transactionService.sumPointsByUserAndEventTypeAndDate(
                eq(user.getUuid()), eq(eventType.getUuid()), eq(LocalDate.now())))
                .thenReturn(250L);  // 250 + 80 = 330 > 300

        AwardResultView result = service.awardPoints("user-123", "evt-001", "quiz", null, null);

        assertThat(result.success()).isFalse();
        assertThat(result.rejectionReason()).contains("Превышен дневной лимит");

        verify(transactionService).sumPointsByUserAndEventTypeAndDate(any(), any(), eq(LocalDate.now()));
    }

    @Test
    void awardPoints_successNoCourse_levelUp() {
        // подготовка моков
        when(eventTypeService.getActiveByCode("quiz")).thenReturn(eventType);
        when(userService.getUserByExternalId("user-123")).thenReturn(user);
        when(transactionService.isExistsByEventId("evt-001")).thenReturn(false);
        when(transactionService.sumPointsByUserAndEventTypeAndDate(any(), any(), any())).thenReturn(100L);
        when(levelCalculator.calculateLevel(500 + 80)).thenReturn(4);
        when(levelCalculator.pointsToNextLevel(4)).thenReturn(200L);

        // вызов
        AwardResultView result = service.awardPoints("user-123", "evt-001", "quiz", null, null);

        // проверки результата
        assertThat(result.success()).isTrue();
        assertThat(result.pointsEarned()).isEqualTo(80);
        assertThat(result.totalPointsAfter()).isEqualTo(580);
        assertThat(result.levelUp()).isTrue();
        assertThat(result.newLevel()).isEqualTo(4);
        assertThat(result.pointsToNextLevel()).isEqualTo(200L);
        assertThat(result.progressPercent()).isEqualTo(100.0);  // т.к. 580 > 200 → min(..., 100)

        // проверка сохранения транзакции
        verify(transactionService).saveIfNotExists(transactionCaptor.capture());
        Transaction tx = transactionCaptor.getValue();
        assertThat(tx.getUser()).isEqualTo(user);
        assertThat(tx.getCourse()).isNull();
        assertThat(tx.getPoints()).isEqualTo(80);
        assertThat(tx.getDescription()).isEqualTo("Начисление за Квиз");

        // проверка обновления пользователя
        verify(userService).update(userCaptor.capture());
        User updated = userCaptor.getValue();
        assertThat(updated.getTotalPoints()).isEqualTo(580);
        assertThat(updated.getLevel()).isEqualTo(4);

        // курс не запрашивался
        verifyNoInteractions(courseService, enrollmentApplicationService);
    }

    @Test
    void awardPoints_successWithCourse_callsAddPoints() {
        when(eventTypeService.getActiveByCode("quiz")).thenReturn(eventType);
        when(userService.getUserByExternalId("user-123")).thenReturn(user);
        when(courseService.findByCourseId("MATH-101")).thenReturn(course);
        when(transactionService.isExistsByEventId("evt-001")).thenReturn(false);
        when(transactionService.sumPointsByUserAndEventTypeAndDate(any(), any(), any())).thenReturn(0L);
        when(levelCalculator.calculateLevel(anyInt())).thenReturn(3);
        when(levelCalculator.pointsToNextLevel(anyInt())).thenReturn(300L);

        AwardResultView result = service.awardPoints("user-123", "evt-001", "quiz", "MATH-101", "G-1");

        assertThat(result.success()).isTrue();
        assertThat(result.pointsEarned()).isEqualTo(80);

        verify(courseService).findByCourseId("MATH-101");
        verify(enrollmentApplicationService).addPointsToCourse("user-123", course.getUuid(), 80);
    }

    @Test
    void awardPoints_success_transactionSavedCorrectly() {
        when(eventTypeService.getActiveByCode("quiz")).thenReturn(eventType);
        when(userService.getUserByExternalId("user-123")).thenReturn(user);
        when(transactionService.isExistsByEventId(any())).thenReturn(false);
        when(transactionService.sumPointsByUserAndEventTypeAndDate(any(), any(), any())).thenReturn(0L);

        service.awardPoints("user-123", "evt-001", "quiz", null, null);

        verify(transactionService).saveIfNotExists(transactionCaptor.capture());
        Transaction tx = transactionCaptor.getValue();

        assertThat(tx.getUser()).isEqualTo(user);
        assertThat(tx.getEventId()).isEqualTo("evt-001");
        assertThat(tx.getPoints()).isEqualTo(80);
        assertThat(tx.getDescription()).isEqualTo("Начисление за Квиз");
        assertThat(tx.getCreatedAt()).isNotNull();
    }
}