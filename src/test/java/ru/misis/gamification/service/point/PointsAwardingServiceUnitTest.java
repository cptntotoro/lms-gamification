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
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.entity.Transaction;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.service.course.UserCourseService;
import ru.misis.gamification.service.event.EventTypeService;
import ru.misis.gamification.service.point.result.AwardResult;
import ru.misis.gamification.service.progress.LevelCalculatorService;
import ru.misis.gamification.service.simple.course.CourseService;
import ru.misis.gamification.service.simple.transaction.TransactionService;
import ru.misis.gamification.service.simple.user.UserService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointsAwardingServiceUnitTest {

    @Mock
    private TransactionService transactionService;
    @Mock
    private EventTypeService eventTypeService;
    @Mock
    private UserService userService;
    @Mock
    private LevelCalculatorService levelCalculatorService;
    @Mock
    private UserCourseService userCourseService;
    @Mock
    private CourseService courseService;

    @InjectMocks
    private PointsAwardingServiceImpl service;

    @Captor
    private ArgumentCaptor<Transaction> txCaptor;
    @Captor
    private ArgumentCaptor<User> userCaptor;

    private LmsEventRequestDto request;
    private User user;
    private EventType eventType;
    private Course course;

    @BeforeEach
    void setUp() {
        request = LmsEventRequestDto.builder()
                .userId("user-123")
                .eventId("evt-999")
                .eventType("quiz")
                .courseId("MATH-101")
                .groupId("PM-21-1")
                .build();

        user = User.builder()
                .uuid(UUID.randomUUID())
                .userId("user-123")
                .totalPoints(1200)
                .level(5)
                .build();

        eventType = EventType.builder()
                .uuid(UUID.randomUUID())
                .typeCode("quiz")
                .displayName("Квиз / Тест")
                .points(100)
                .maxDailyPoints(500)
                .active(true)
                .build();

        course = Course.builder()
                .uuid(UUID.randomUUID())
                .courseId("MATH-101")
                .build();

        lenient().when(userService.createIfNotExists(anyString(), any(), any())).thenReturn(user);
    }

    @Test
    void awardPoints_successfulAward_savesTransaction_updatesUser_addsCoursePoints() {
        when(transactionService.isExistsByEventId("evt-999")).thenReturn(false);
        when(eventTypeService.getActiveByCode("quiz")).thenReturn(eventType);
        when(transactionService.sumPointsByUserAndEventTypeAndDate(any(), any(), any())).thenReturn(300L);
        when(transactionService.saveIfNotExists(any(Transaction.class))).thenAnswer(inv -> {
            Transaction tx = inv.getArgument(0);
            tx.setUuid(UUID.randomUUID());
            return tx;
        });
        when(levelCalculatorService.calculateLevel(1300)).thenReturn(5);
        when(courseService.findByCourseId("MATH-101")).thenReturn(course);

        AwardResult result = service.awardPoints(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPointsEarned()).isEqualTo(100);
        assertThat(result.getTotalPointsAfter()).isEqualTo(1300);

        verify(transactionService).saveIfNotExists(txCaptor.capture());
        Transaction tx = txCaptor.getValue();
        assertThat(tx.getUser()).isEqualTo(user);
        assertThat(tx.getCourse()).isEqualTo(course);
        assertThat(tx.getEventType()).isEqualTo(eventType);
        assertThat(tx.getEventId()).isEqualTo("evt-999");
        assertThat(tx.getPoints()).isEqualTo(100);
        assertThat(tx.getDescription()).contains("Квиз / Тест");

        verify(userService).update(userCaptor.capture());
        assertThat(userCaptor.getValue().getTotalPoints()).isEqualTo(1300);

        verify(userCourseService).addPointsToCourse(eq(user), eq(course.getUuid()), eq(100));
    }

    @Test
    void awardPoints_duplicateEvent_returnsDuplicate() {
        when(transactionService.isExistsByEventId("evt-999")).thenReturn(true);

        AwardResult result = service.awardPoints(request);

        assertThat(result.isDuplicate()).isTrue();
        verify(transactionService).isExistsByEventId("evt-999");
        verifyNoMoreInteractions(eventTypeService, userService, transactionService, userCourseService, courseService);
    }

    @Test
    void awardPoints_eventTypeNotFound_returnsRejected() {
        when(transactionService.isExistsByEventId(any())).thenReturn(false);
        when(eventTypeService.getActiveByCode("quiz"))
                .thenThrow(new EventTypeNotFoundException("quiz"));

        AwardResult result = service.awardPoints(request);

        assertThat(result.isRejected()).isTrue();
        assertThat(result.getRejectionReason()).contains("Неизвестный или отключённый тип события");
        verify(eventTypeService).getActiveByCode("quiz");
        verifyNoMoreInteractions(transactionService, userService, userCourseService, courseService);
    }

    @Test
    void awardPoints_dailyLimitExceeded_returnsRejected() {
        when(transactionService.isExistsByEventId(any())).thenReturn(false);
        when(eventTypeService.getActiveByCode(any())).thenReturn(eventType);
        when(userService.createIfNotExists(any(), any(), any())).thenReturn(user);
        when(transactionService.sumPointsByUserAndEventTypeAndDate(any(), any(), any())).thenReturn(450L);

        AwardResult result = service.awardPoints(request);

        assertThat(result.isRejected()).isTrue();
        assertThat(result.getRejectionReason()).contains("Превышен дневной лимит");
        verifyNoMoreInteractions(userService, userCourseService);
    }

    @Test
    void awardPoints_noCourseId_skipsCourseAddition() {
        request.setCourseId(null);

        when(transactionService.isExistsByEventId(any())).thenReturn(false);
        when(eventTypeService.getActiveByCode(any())).thenReturn(eventType);
        when(userService.createIfNotExists(eq("user-123"), eq(null), eq("PM-21-1"))).thenReturn(user);
        when(transactionService.sumPointsByUserAndEventTypeAndDate(any(), any(), any())).thenReturn(0L);
        when(transactionService.saveIfNotExists(any())).thenAnswer(inv -> inv.getArgument(0));
        when(levelCalculatorService.calculateLevel(anyInt())).thenReturn(5);

        AwardResult result = service.awardPoints(request);

        assertThat(result.isSuccess()).isTrue();
        verify(userCourseService, never()).addPointsToCourse(any(), any(), anyInt());
    }

    @Test
    void awardPoints_userIdMissing_returnsRejected() {
        request.setUserId(null);

        AwardResult result = service.awardPoints(request);

        assertThat(result.isRejected()).isTrue();
        assertThat(result.getRejectionReason()).contains("Отсутствует идентификатор пользователя");
        verifyNoInteractions(eventTypeService, transactionService, userService, userCourseService, courseService);
    }
}