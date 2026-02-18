package ru.misis.gamification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.dto.lms.response.LmsEventResponsetDto;
import ru.misis.gamification.exception.DuplicateEventException;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.model.entity.LmsEvent;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.service.transaction.TransactionService;
import ru.misis.gamification.service.user.UserService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
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

    @InjectMocks
    private EventManagementServiceImpl service;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User baseUser;

    @BeforeEach
    void setUp() {
        baseUser = User.builder()
                .uuid(UUID.randomUUID())
                .userId("user-test")
                .totalPoints(150)
                .level(2)
                .build();
    }

    @Test
    void success_newEvent_updatesUserAndReturnsSuccess() {
        LmsEvent event = LmsEvent.builder()
                .userId("user-test")
                .eventId("evt-001")
                .pointsEarned(80)
                .build();

        when(userService.createIfNotExists("user-test")).thenReturn(baseUser);

        Transaction savedTx = Transaction.builder()
                .uuid(UUID.randomUUID())
                .userId("user-test")
                .eventId("evt-001")
                .pointsEarned(80)
                .build();

        when(transactionService.saveIfNotExists(any())).thenReturn(savedTx);

        User updatedUser = User.builder()
                .uuid(baseUser.getUuid())
                .userId("user-test")
                .totalPoints(230)
                .level(3)
                .build();

        when(userService.update(any())).thenReturn(updatedUser);

        LmsEventResponsetDto response = service.process(event);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getEventId()).isEqualTo("evt-001");
        assertThat(response.getTotalPoints()).isEqualTo(230);

        assertThat(response.isDuplicate()).isFalse();
        assertThat(response.isError()).isFalse();

        verify(userService).update(userCaptor.capture());
        assertThat(userCaptor.getValue().getTotalPoints()).isEqualTo(230);
        assertThat(userCaptor.getValue().getLevel()).isEqualTo(3);
    }

    @Test
    void duplicateEvent_returnsDuplicateWithoutUpdate() {
        LmsEvent event = LmsEvent.builder()
                .userId("user-test")
                .eventId("evt-dup")
                .pointsEarned(100)
                .build();

        when(userService.createIfNotExists(anyString())).thenReturn(baseUser);

        doThrow(new DuplicateEventException("evt-dup"))
                .when(transactionService).saveIfNotExists(any());

        LmsEventResponsetDto response = service.process(event);

        assertThat(response.isDuplicate()).isTrue();
        assertThat(response.getEventId()).isEqualTo("evt-dup");
        assertThat(response.getMessage()).contains("уже обработано");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.isError()).isFalse();

        verify(userService, never()).update(any());
    }

    @Test
    void invalidPoints_returnsErrorWithoutInteractions() {
        LmsEvent event = LmsEvent.builder()
                .userId("user-test")
                .eventId("evt-bad")
                .pointsEarned(-10)
                .build();

        LmsEventResponsetDto response = service.process(event);

        assertThat(response.isError()).isTrue();
        assertThat(response.getMessage()).contains("положительным");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.isDuplicate()).isFalse();

        verifyNoInteractions(userService, transactionService);
    }

    @Test
    void nullEvent_returnsError() {
        LmsEventResponsetDto result = service.process(null);

        assertThat(result.getStatus()).isEqualTo("error");
        assertThat(result.getMessage()).contains("не может быть null");

        verifyNoInteractions(userService, transactionService);
    }

    @Test
    void blankUserId_returnsError() {
        LmsEvent event = LmsEvent.builder()
                .userId("   ")
                .eventId("evt")
                .pointsEarned(50)
                .build();

        LmsEventResponsetDto result = service.process(event);

        assertThat(result.getStatus()).isEqualTo("error");
        assertThat(result.getMessage()).contains("обязателен");

        verifyNoInteractions(userService, transactionService);
    }
}