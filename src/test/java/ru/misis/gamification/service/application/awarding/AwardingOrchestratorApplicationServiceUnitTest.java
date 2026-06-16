package ru.misis.gamification.service.application.awarding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.entity.*;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.model.AwardResultView;
import ru.misis.gamification.model.EnrollmentResult;
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
    private Group group;

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
        
        group = Group.builder()
                .uuid(UUID.randomUUID())
                .groupId("G-1")
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
                .thenThrow(new EventTypeNotFoundException("Активный тип события не найден по коду: quiz"));

        AwardResultView result = service.awardPoints("user-123", "evt-001", "quiz", "MATH-101", "G-1");

        assertThat(result.success()).isFalse();
        assertThat(result.duplicate()).isFalse();
        assertThat(result.rejectionReason()).contains("Неизвестный или отключённый тип события: quiz");

        verify(eventTypeService).getActiveByCode("quiz");
        // Проверяем, что enrollmentApplicationService не вызывался
        verifyNoInteractions(enrollmentApplicationService);
    }

    @Test
    void awardPoints_dailyLimitExceeded_returnsRejected() {
        when(eventTypeService.getActiveByCode("quiz")).thenReturn(eventType);
        when(userService.createIfNotExists("user-123")).thenReturn(user);
        when(transactionService.isExistsByEventId(any())).thenReturn(false);
        when(transactionService.sumPointsByUserAndEventTypeAndDate(
                eq(user.getUuid()), eq(eventType.getUuid()), eq(LocalDate.now())))
                .thenReturn(250L);  // 250 + 80 = 330 > 300

        when(enrollmentApplicationService.enrollIfNeeded("user-123", "MATH-101", "G-1"))
                .thenReturn(new EnrollmentResult(course, group));
        when(enrollmentApplicationService.isUserInGroup(user, group)).thenReturn(true);

        AwardResultView result = service.awardPoints("user-123", "evt-001", "quiz", "MATH-101", "G-1");

        assertThat(result.success()).isFalse();
        assertThat(result.rejectionReason()).contains("Превышен дневной лимит");

        verify(transactionService).sumPointsByUserAndEventTypeAndDate(any(), any(), eq(LocalDate.now()));
    }

    @Test
    void awardPoints_successWithCourseAndGroup_levelUp() {
        // Успешный сценарий с курсом и группой, происходит повышение уровня
        when(eventTypeService.getActiveByCode("quiz")).thenReturn(eventType);
        when(userService.createIfNotExists("user-123")).thenReturn(user);
        when(transactionService.isExistsByEventId("evt-001")).thenReturn(false);
        when(transactionService.sumPointsByUserAndEventTypeAndDate(any(), any(), any())).thenReturn(100L);
        when(levelCalculator.calculateLevel(500 + 80)).thenReturn(4);
        when(levelCalculator.pointsToNextLevel(4)).thenReturn(200L);

        when(enrollmentApplicationService.enrollIfNeeded("user-123", "MATH-101", "G-1"))
                .thenReturn(new EnrollmentResult(course, group));
        when(enrollmentApplicationService.isUserInGroup(user, group)).thenReturn(true);

        AwardResultView result = service.awardPoints("user-123", "evt-001", "quiz", "MATH-101", "G-1");

        // проверки результата
        assertThat(result.success()).isTrue();
        assertThat(result.pointsEarned()).isEqualTo(80);
        assertThat(result.totalPointsAfter()).isEqualTo(580);
        assertThat(result.levelUp()).isTrue();
        assertThat(result.newLevel()).isEqualTo(4);
        assertThat(result.pointsToNextLevel()).isEqualTo(200L);
        assertThat(result.progressPercent()).isEqualTo(100.0);  // т.к. 580 > 200 → ограничено 100

        // проверка сохранения транзакции
        verify(transactionService).saveIfNotExists(transactionCaptor.capture());
        Transaction tx = transactionCaptor.getValue();
        assertThat(tx.getUser()).isEqualTo(user);
        assertThat(tx.getCourse()).isEqualTo(course);
        assertThat(tx.getGroup()).isEqualTo(group);
        assertThat(tx.getPoints()).isEqualTo(80);
        assertThat(tx.getDescription()).isEqualTo("Начисление за Квиз");

        // Проверка обновления пользователя
        verify(userService).update(userCaptor.capture());
        User updated = userCaptor.getValue();
        assertThat(updated.getTotalPoints()).isEqualTo(580);
        assertThat(updated.getLevel()).isEqualTo(4);

        // Проверяем, что enrolment-сервис вызван для зачисления и добавления очков на курс
        verify(enrollmentApplicationService).enrollIfNeeded("user-123", "MATH-101", "G-1");
        verify(enrollmentApplicationService).isUserInGroup(user, group);
        verify(enrollmentApplicationService).addPointsToCourse("user-123", course.getUuid(), 80);

        // CourseService не должен вызываться напрямую, т.к. курс берётся из EnrollmentResult
        verifyNoInteractions(courseService);
    }

    @Test
    void awardPoints_successWithCourse_addPointsToCourseCalled() {
        when(eventTypeService.getActiveByCode("quiz")).thenReturn(eventType);
        when(userService.createIfNotExists("user-123")).thenReturn(user);
        when(transactionService.isExistsByEventId("evt-001")).thenReturn(false);
        when(transactionService.sumPointsByUserAndEventTypeAndDate(any(), any(), any())).thenReturn(0L);
        when(levelCalculator.calculateLevel(anyInt())).thenReturn(3);
        when(levelCalculator.pointsToNextLevel(anyInt())).thenReturn(300L);

        when(enrollmentApplicationService.enrollIfNeeded("user-123", "MATH-101", "G-1"))
                .thenReturn(new EnrollmentResult(course, group));
        when(enrollmentApplicationService.isUserInGroup(user, group)).thenReturn(true);

        AwardResultView result = service.awardPoints("user-123", "evt-001", "quiz", "MATH-101", "G-1");

        assertThat(result.success()).isTrue();
        assertThat(result.pointsEarned()).isEqualTo(80);

        // Проверяем, что addPointsToCourse вызван с правильными параметрами
        verify(enrollmentApplicationService).addPointsToCourse("user-123", course.getUuid(), 80);
        // Проверяем, что courseService напрямую не вызывается
        verifyNoInteractions(courseService);
    }

    @Test
    void awardPoints_success_transactionSavedCorrectly() {
        when(eventTypeService.getActiveByCode("quiz")).thenReturn(eventType);
        when(userService.createIfNotExists("user-123")).thenReturn(user);
        when(transactionService.isExistsByEventId(any())).thenReturn(false);
        when(transactionService.sumPointsByUserAndEventTypeAndDate(any(), any(), any())).thenReturn(0L);
        when(levelCalculator.calculateLevel(anyInt())).thenReturn(3);
        when(levelCalculator.pointsToNextLevel(anyInt())).thenReturn(300L);

        when(enrollmentApplicationService.enrollIfNeeded("user-123", "MATH-101", "G-1"))
                .thenReturn(new EnrollmentResult(course, group));
        when(enrollmentApplicationService.isUserInGroup(user, group)).thenReturn(true);

        service.awardPoints("user-123", "evt-001", "quiz", "MATH-101", "G-1");

        verify(transactionService).saveIfNotExists(transactionCaptor.capture());
        Transaction tx = transactionCaptor.getValue();

        assertThat(tx.getUser()).isEqualTo(user);
        assertThat(tx.getEventId()).isEqualTo("evt-001");
        assertThat(tx.getPoints()).isEqualTo(80);
        assertThat(tx.getDescription()).isEqualTo("Начисление за Квиз");
        assertThat(tx.getCreatedAt()).isNotNull();
        assertThat(tx.getCourse()).isEqualTo(course);
        assertThat(tx.getGroup()).isEqualTo(group);
    }
}