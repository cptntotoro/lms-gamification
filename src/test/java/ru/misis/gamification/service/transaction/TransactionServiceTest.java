package ru.misis.gamification.service.transaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.misis.gamification.exception.DuplicateEventException;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.repository.TransactionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void isExistsByEventId_true() {
        when(transactionRepository.existsByEventId("evt-123")).thenReturn(true);

        boolean result = transactionService.isExistsByEventId("evt-123");

        assertThat(result).isTrue();
        verify(transactionRepository).existsByEventId("evt-123");
    }

    @Test
    void isExistsByEventId_false() {
        when(transactionRepository.existsByEventId("evt-new")).thenReturn(false);

        boolean result = transactionService.isExistsByEventId("evt-new");

        assertThat(result).isFalse();
        verify(transactionRepository).existsByEventId("evt-new");
    }

    @Test
    void saveIfNotExists_uniqueEvent_savesAndReturns() {
        Transaction tx = Transaction.builder()
                .eventId("evt-unique")
                .userId("u-001")
                .pointsEarned(150)
                .build();

        Transaction saved = Transaction.builder()
                .uuid(UUID.randomUUID())
                .eventId("evt-unique")
                .build();

        when(transactionRepository.existsByEventId("evt-unique")).thenReturn(false);
        when(transactionRepository.save(tx)).thenReturn(saved);

        Transaction result = transactionService.saveIfNotExists(tx);

        assertThat(result).isSameAs(saved);

        verify(transactionRepository).existsByEventId("evt-unique");
        verify(transactionRepository).save(tx);
    }

    @Test
    void saveIfNotExists_duplicateByCheck_throwsDuplicateEventException() {
        Transaction tx = Transaction.builder()
                .eventId("evt-dup")
                .build();

        when(transactionRepository.existsByEventId("evt-dup")).thenReturn(true);

        assertThatThrownBy(() -> transactionService.saveIfNotExists(tx))
                .isInstanceOf(DuplicateEventException.class)
                .hasMessageContaining("evt-dup");

        verify(transactionRepository).existsByEventId("evt-dup");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void saveIfNotExists_duplicateByConstraint_throwsDuplicateEventException() {
        Transaction tx = Transaction.builder()
                .eventId("evt-constraint")
                .build();

        when(transactionRepository.existsByEventId("evt-constraint")).thenReturn(false);
        when(transactionRepository.save(tx))
                .thenThrow(new DataIntegrityViolationException("unique constraint violation"));

        assertThatThrownBy(() -> transactionService.saveIfNotExists(tx))
                .isInstanceOf(DuplicateEventException.class)
                .hasMessageContaining("evt-constraint");

        verify(transactionRepository).existsByEventId("evt-constraint");
        verify(transactionRepository).save(tx);
    }

    @Test
    void saveIfNotExists_otherDataIntegrityViolation_rethrowsAsIs() {
        Transaction tx = Transaction.builder()
                .eventId("evt-other-violation")
                .build();

        DataIntegrityViolationException originalEx = new DataIntegrityViolationException("foreign key violation");

        when(transactionRepository.existsByEventId(anyString())).thenReturn(false);
        when(transactionRepository.save(tx)).thenThrow(originalEx);

        assertThatThrownBy(() -> transactionService.saveIfNotExists(tx))
                .isInstanceOf(DuplicateEventException.class)
                .hasMessageContaining("evt-other-violation");

        verify(transactionRepository).existsByEventId("evt-other-violation");
        verify(transactionRepository).save(tx);
    }

    @Test
    void saveIfNotExists_unexpectedRuntimeException_rethrows() {
        Transaction tx = Transaction.builder()
                .eventId("evt-db-down")
                .build();

        RuntimeException ex = new RuntimeException("Database connection failed");

        when(transactionRepository.existsByEventId(anyString())).thenReturn(false);
        when(transactionRepository.save(tx)).thenThrow(ex);

        assertThatThrownBy(() -> transactionService.saveIfNotExists(tx))
                .isSameAs(ex);

        verify(transactionRepository).existsByEventId("evt-db-down");
        verify(transactionRepository).save(tx);
    }

    @Test
    void getTransactionsByUserId_delegatesToRepository() {
        String userId = "user-page";
        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());

        Page<Transaction> mockPage = new PageImpl<>(
                List.of(new Transaction(), new Transaction()),
                pageable,
                42
        );

        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
                .thenReturn(mockPage);

        Page<Transaction> result = transactionService.getTransactionsByUserId(userId, pageable);

        assertThat(result).isSameAs(mockPage);
        assertThat(result.getTotalElements()).isEqualTo(42);
        assertThat(result.getNumberOfElements()).isEqualTo(2);

        verify(transactionRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Test
    void sumPointsByUserIdAndEventTypeAndDate_delegatesToRepository() {
        String userId = "u-555";
        String typeCode = "quiz";
        LocalDate date = LocalDate.of(2026, 2, 23);

        when(transactionRepository.sumPointsByUserIdAndEventTypeAndDate(userId, typeCode, date))
                .thenReturn(850L);

        long sum = transactionService.sumPointsByUserIdAndEventTypeAndDate(userId, typeCode, date);

        assertThat(sum).isEqualTo(850L);
        verify(transactionRepository).sumPointsByUserIdAndEventTypeAndDate(userId, typeCode, date);
    }

    @Test
    void sumPointsByUserIdAndEventTypeAndDate_zeroWhenNoRecords() {
        when(transactionRepository.sumPointsByUserIdAndEventTypeAndDate(anyString(), anyString(), any()))
                .thenReturn(0L);

        long sum = transactionService.sumPointsByUserIdAndEventTypeAndDate("u", "t", LocalDate.now());

        assertThat(sum).isZero();
    }
}