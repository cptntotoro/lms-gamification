package ru.misis.gamification.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.dto.lms.response.LmsEventResponsetDto;
import ru.misis.gamification.exception.DuplicateEventException;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.service.event.EventTypeService;
import ru.misis.gamification.service.transaction.TransactionService;
import ru.misis.gamification.service.user.UserService;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventManagementServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private EventTypeService eventTypeService;

    @InjectMocks
    private EventManagementServiceImpl service;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<Transaction> txCaptor;

    private User testUser;
    private EventType testEventType;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .uuid(UUID.randomUUID())
                .userId("test-user-123")
                .totalPoints(150)
                .level(2)
                .build();

        testEventType = EventType.builder()
                .typeCode("quiz")
                .displayName("Тест/Квиз")
                .points(80)
                .maxDailyPoints(300)
                .active(true)
                .build();
    }

    @Test
    void process_successNewEvent_updatesUserAndReturnsSuccess() {
        LmsEventRequestDto request = LmsEventRequestDto.builder()
                .userId("test-user-123")
                .eventId("evt-001")
                .eventType("quiz")
                .build();

        when(eventTypeService.getActiveByCode("quiz")).thenReturn(testEventType);
        when(eventTypeService.canAwardPoints(eq("test-user-123"), eq("quiz"), eq(80), any(LocalDate.class)))
                .thenReturn(true);
        when(userService.createIfNotExists("test-user-123")).thenReturn(testUser);

        Transaction savedTx = Transaction.builder()
                .uuid(UUID.randomUUID())
                .userId("test-user-123")
                .eventId("evt-001")
                .eventTypeCode("quiz")
                .pointsEarned(80)
                .build();

        when(transactionService.saveIfNotExists(any())).thenReturn(savedTx);

        User updatedUser = User.builder()
                .uuid(testUser.getUuid())
                .userId("test-user-123")
                .totalPoints(230)
                .level(3)
                .build();

        when(userService.update(any())).thenReturn(updatedUser);

        LmsEventResponsetDto response = service.process(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getUserId()).isEqualTo("test-user-123");
        assertThat(response.getPointsEarned()).isEqualTo(80);
        assertThat(response.getTotalPoints()).isEqualTo(230);
        assertThat(response.getEventId()).isEqualTo("evt-001");
        assertThat(response.getDisplayName()).isEqualTo("Тест/Квиз");

        // Проверяем вызовы
        verify(eventTypeService).getActiveByCode("quiz");
        verify(eventTypeService).canAwardPoints("test-user-123", "quiz", 80, LocalDate.now());
        verify(userService).createIfNotExists("test-user-123");

        verify(transactionService).saveIfNotExists(txCaptor.capture());
        Transaction capturedTx = txCaptor.getValue();
        assertThat(capturedTx.getEventId()).isEqualTo("evt-001");
        assertThat(capturedTx.getEventTypeCode()).isEqualTo("quiz");
        assertThat(capturedTx.getPointsEarned()).isEqualTo(80);
        assertThat(capturedTx.getDescription()).isEqualTo("Тип события: Тест/Квиз");

        verify(userService).update(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getTotalPoints()).isEqualTo(230);
        assertThat(capturedUser.getLevel()).isEqualTo(3);
    }

    @Test
    void process_duplicateEvent_returnsDuplicateWithoutUpdate() {
        LmsEventRequestDto request = LmsEventRequestDto.builder()
                .userId("test-user-123")
                .eventId("evt-dup")
                .eventType("quiz")
                .build();

        when(eventTypeService.getActiveByCode("quiz")).thenReturn(testEventType);
        when(eventTypeService.canAwardPoints(anyString(), anyString(), anyInt(), any(LocalDate.class)))
                .thenReturn(true);
        when(userService.createIfNotExists(anyString())).thenReturn(testUser);

        doThrow(new DuplicateEventException("evt-dup"))
                .when(transactionService).saveIfNotExists(any());

        LmsEventResponsetDto response = service.process(request);

        assertThat(response.isDuplicate()).isTrue();
        assertThat(response.getEventId()).isEqualTo("evt-dup");
        assertThat(response.getMessage()).contains("уже обработано");

        verify(userService, never()).update(any());
    }

    @Test
    void process_dailyLimitExceeded_returnsErrorWithoutTransaction() {
        LmsEventRequestDto request = LmsEventRequestDto.builder()
                .userId("test-user-123")
                .eventId("evt-limit")
                .eventType("quiz")
                .build();

        when(eventTypeService.getActiveByCode("quiz")).thenReturn(testEventType);
        when(eventTypeService.canAwardPoints("test-user-123", "quiz", 80, LocalDate.now()))
                .thenReturn(false);

        LmsEventResponsetDto response = service.process(request);

        assertThat(response.isError()).isTrue();
        assertThat(response.getMessage()).contains("дневной лимит");

        verifyNoInteractions(transactionService);
        verify(userService, never()).update(any(User.class));
    }

    @Test
    void process_unknownEventType_returnsError() {
        LmsEventRequestDto request = LmsEventRequestDto.builder()
                .userId("test-user-123")
                .eventId("evt-unknown")
                .eventType("unknown-type")
                .build();

        when(eventTypeService.getActiveByCode("unknown-type"))
                .thenThrow(new EntityNotFoundException("Не найден"));

        LmsEventResponsetDto response = service.process(request);

        assertThat(response.isError()).isTrue();
        assertThat(response.getMessage()).contains("Неизвестный или отключённый тип события");

        verifyNoInteractions(transactionService, userService);
    }

    @Test
    void process_nullRequest_returnsError() {
        LmsEventResponsetDto response = service.process(null);

        assertThat(response.isError()).isTrue();
        assertThat(response.getMessage()).contains("не может быть null");

        verifyNoInteractions(eventTypeService, transactionService, userService);
    }

    @Test
    void process_blankUserId_returnsError() {
        LmsEventRequestDto request = LmsEventRequestDto.builder()
                .userId("   ")
                .eventId("evt-001")
                .eventType("quiz")
                .build();

        LmsEventResponsetDto response = service.process(request);

        assertThat(response.isError()).isTrue();
        assertThat(response.getMessage()).contains("Идентификатор пользователя обязателен");

        verifyNoInteractions(eventTypeService, transactionService, userService);
    }

    @Test
    void process_blankEventId_returnsError() {
        LmsEventRequestDto request = LmsEventRequestDto.builder()
                .userId("test-user")
                .eventId("")
                .eventType("quiz")
                .build();

        LmsEventResponsetDto response = service.process(request);

        assertThat(response.isError()).isTrue();
        assertThat(response.getMessage()).contains("Идентификатор события обязателен");

        verifyNoInteractions(eventTypeService, transactionService, userService);
    }

    @Test
    void process_blankEventType_returnsError() {
        LmsEventRequestDto request = LmsEventRequestDto.builder()
                .userId("test-user")
                .eventId("evt-001")
                .eventType("  ")
                .build();

        LmsEventResponsetDto response = service.process(request);

        assertThat(response.isError()).isTrue();
        assertThat(response.getMessage()).contains("Тип события (eventType) обязателен");

        verifyNoInteractions(eventTypeService, transactionService, userService);
    }

    @Test
    void process_unexpectedSaveError_returnsError() {
        LmsEventRequestDto request = LmsEventRequestDto.builder()
                .userId("test-user-123")
                .eventId("evt-error")
                .eventType("quiz")
                .build();

        when(eventTypeService.getActiveByCode("quiz")).thenReturn(testEventType);
        when(eventTypeService.canAwardPoints(anyString(), anyString(), anyInt(), any(LocalDate.class)))
                .thenReturn(true);
        when(userService.createIfNotExists(anyString())).thenReturn(testUser);

        RuntimeException ex = new RuntimeException("DB down");
        doThrow(ex).when(transactionService).saveIfNotExists(any());

        LmsEventResponsetDto response = service.process(request);

        assertThat(response.isError()).isTrue();
        assertThat(response.getMessage()).contains("Внутренняя ошибка сервера");

        verify(userService, never()).update(any());
    }
}