package ru.misis.gamification.service.simple.transaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.misis.gamification.entity.Transaction;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.exception.DuplicateEventException;
import ru.misis.gamification.repository.TransactionRepository;
import ru.misis.gamification.service.simple.user.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceUnitTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransactionServiceImpl service;

    @Captor
    private ArgumentCaptor<Transaction> txCaptor;

    private Transaction createValidTransaction() {
        User user = User.builder()
                .uuid(UUID.randomUUID())
                .userId("user-123")
                .build();

        return Transaction.builder()
                .eventId("evt-unique-001")
                .user(user)
                .points(150)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void isExistsByEventId_existingEvent_returnsTrue() {
        when(transactionRepository.existsByEventId("evt-123")).thenReturn(true);

        boolean exists = service.isExistsByEventId("evt-123");

        assertThat(exists).isTrue();
        verify(transactionRepository).existsByEventId("evt-123");
    }

    @Test
    void isExistsByEventId_nonExistingEvent_returnsFalse() {
        when(transactionRepository.existsByEventId("evt-new")).thenReturn(false);

        boolean exists = service.isExistsByEventId("evt-new");

        assertThat(exists).isFalse();
        verify(transactionRepository).existsByEventId("evt-new");
    }

    @Test
    void saveIfNotExists_uniqueEvent_savesAndReturns() {
        Transaction tx = createValidTransaction();

        when(transactionRepository.existsByEventId(tx.getEventId())).thenReturn(false);

        when(transactionRepository.save(tx)).thenAnswer(invocation -> {
            Transaction argument = invocation.getArgument(0);
            argument.setUuid(UUID.randomUUID());
            return argument;
        });

        Transaction saved = service.saveIfNotExists(tx);

        assertThat(saved.getEventId()).isEqualTo(tx.getEventId());
        assertThat(saved.getUuid()).isNotNull();

        verify(transactionRepository).existsByEventId(tx.getEventId());
        verify(transactionRepository).save(txCaptor.capture());

        assertThat(txCaptor.getValue()).isSameAs(tx);
    }

    @Test
    void saveIfNotExists_duplicateByCheck_throwsDuplicateEventException() {
        Transaction tx = createValidTransaction();
        tx.setEventId("evt-dup");

        when(transactionRepository.existsByEventId("evt-dup")).thenReturn(true);

        assertThatThrownBy(() -> service.saveIfNotExists(tx))
                .isInstanceOf(DuplicateEventException.class)
                .hasMessageContaining("evt-dup");

        verify(transactionRepository).existsByEventId("evt-dup");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void saveIfNotExists_duplicateByConstraint_throwsDuplicateEventException() {
        Transaction tx = createValidTransaction();
        tx.setEventId("evt-constraint");

        when(transactionRepository.existsByEventId("evt-constraint")).thenReturn(false);
        when(transactionRepository.save(any())).thenThrow(
                new DataIntegrityViolationException("unique constraint violation"));

        assertThatThrownBy(() -> service.saveIfNotExists(tx))
                .isInstanceOf(DuplicateEventException.class)
                .hasMessageContaining("evt-constraint");

        verify(transactionRepository).existsByEventId("evt-constraint");
        verify(transactionRepository).save(any());
    }

    @Test
    void saveIfNotExists_nullTransaction_throwsIllegalArgument() {
        assertThatThrownBy(() -> service.saveIfNotExists(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Транзакция не может быть null");
    }

    @Test
    void saveIfNotExists_nullEventId_throwsIllegalArgument() {
        Transaction invalid = createValidTransaction();
        invalid.setEventId(null);

        assertThatThrownBy(() -> service.saveIfNotExists(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("eventId не может быть null или пустым");
    }

    @Test
    void saveIfNotExists_blankEventId_throwsIllegalArgument() {
        Transaction invalid = createValidTransaction();
        invalid.setEventId("   ");

        assertThatThrownBy(() -> service.saveIfNotExists(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("eventId не может быть null или пустым");
    }

    @Test
    void saveIfNotExists_nullUser_throwsIllegalArgument() {
        Transaction invalid = createValidTransaction();
        invalid.setUser(null);

        assertThatThrownBy(() -> service.saveIfNotExists(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Пользователь или его UUID не может быть null");
    }

    @Test
    void saveIfNotExists_nullUserUuid_throwsIllegalArgument() {
        User userWithoutUuid = User.builder().userId("user-123").build();
        Transaction invalid = createValidTransaction();
        invalid.setUser(userWithoutUuid);

        assertThatThrownBy(() -> service.saveIfNotExists(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Пользователь или его UUID не может быть null");
    }

    @Test
    void saveIfNotExists_nullPoints_throwsIllegalArgument() {
        Transaction invalid = createValidTransaction();
        invalid.setPoints(null);

        assertThatThrownBy(() -> service.saveIfNotExists(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Количество очков не может быть null или отрицательным");
    }

    @Test
    void saveIfNotExists_negativePoints_throwsIllegalArgument() {
        Transaction invalid = createValidTransaction();
        invalid.setPoints(-50);

        assertThatThrownBy(() -> service.saveIfNotExists(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Количество очков не может быть null или отрицательным");
    }

    @Test
    void getTransactionsByUserId_returnsPageFromRepository() {
        String userId = "user-page";
        UUID userUuid = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());

        List<Transaction> content = List.of(
                new Transaction(), new Transaction(), new Transaction(),
                new Transaction(), new Transaction()
        );
        Page<Transaction> mockPage = new PageImpl<>(content, pageable, 5);

        when(userService.getUserUuidByExternalId(userId)).thenReturn(userUuid);
        when(transactionRepository.findByUserUuidOrderByCreatedAtDesc(userUuid, pageable))
                .thenReturn(mockPage);

        Page<Transaction> result = service.getTransactionsByUserId(userId, pageable);

        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getNumberOfElements()).isEqualTo(5);
        assertThat(result.getContent()).hasSize(5);

        verify(userService).getUserUuidByExternalId(userId);
        verify(transactionRepository).findByUserUuidOrderByCreatedAtDesc(userUuid, pageable);
    }

    @Test
    void sumPointsByUserAndEventTypeAndDate_returnsSumFromRepository() {
        UUID userUuid = UUID.randomUUID();
        UUID eventTypeUuid = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 3, 1);

        when(transactionRepository.sumPointsByUserUuidAndEventTypeUuidAndDate(userUuid, eventTypeUuid, date))
                .thenReturn(850L);

        long sum = service.sumPointsByUserAndEventTypeAndDate(userUuid, eventTypeUuid, date);

        assertThat(sum).isEqualTo(850L);
        verify(transactionRepository).sumPointsByUserUuidAndEventTypeUuidAndDate(userUuid, eventTypeUuid, date);
    }
}