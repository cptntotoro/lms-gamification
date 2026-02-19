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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void saveIfNotExists_uniqueEvent_shouldSaveAndReturn() {
        Transaction tx = Transaction.builder()
                .eventId("evt-unique-001")
                .userId("u-001")
                .pointsEarned(100)
                .build();

        when(transactionRepository.existsByEventId("evt-unique-001")).thenReturn(false);

        Transaction saved = Transaction.builder()
                .uuid(UUID.randomUUID())
                .eventId("evt-unique-001")
                .build();

        when(transactionRepository.save(tx)).thenReturn(saved);

        Transaction result = transactionService.saveIfNotExists(tx);

        assertThat(result).isSameAs(saved);
        verify(transactionRepository).existsByEventId("evt-unique-001");
        verify(transactionRepository).save(tx);
    }

    @Test
    void saveIfNotExists_duplicateEvent_shouldThrowException() {
        Transaction tx = Transaction.builder()
                .eventId("evt-dup-777")
                .build();

        when(transactionRepository.existsByEventId("evt-dup-777")).thenReturn(true);

        assertThatThrownBy(() -> transactionService.saveIfNotExists(tx))
                .isInstanceOf(DuplicateEventException.class)
                .hasMessageContaining("evt-dup-777");

        verify(transactionRepository).existsByEventId("evt-dup-777");
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    void saveIfNotExists_unexpectedConstraintViolation_shouldRethrow() {
        Transaction tx = Transaction.builder()
                .eventId("evt-999")
                .userId("u-999")
                .pointsEarned(300)
                .build();

        when(transactionRepository.existsByEventId("evt-999")).thenReturn(false);
        when(transactionRepository.save(tx)).thenThrow(new DataIntegrityViolationException("other constraint"));

        assertThatThrownBy(() -> transactionService.saveIfNotExists(tx))
                .isInstanceOf(DuplicateEventException.class);

        verify(transactionRepository).existsByEventId("evt-999");
        verify(transactionRepository).save(tx);
    }

    @Test
    void saveIfNotExists_unexpectedException_shouldLogAndRethrow() {
        Transaction tx = Transaction.builder()
                .eventId("evt-error")
                .build();

        when(transactionRepository.existsByEventId("evt-error")).thenReturn(false);
        RuntimeException ex = new RuntimeException("DB is down");
        when(transactionRepository.save(tx)).thenThrow(ex);

        assertThatThrownBy(() -> transactionService.saveIfNotExists(tx))
                .isSameAs(ex);

        verify(transactionRepository).existsByEventId("evt-error");
        verify(transactionRepository).save(tx);
    }

    @Test
    void getTransactionsByUserId_shouldDelegateToRepository() {
        String userId = "user-page";
        Pageable pageable = PageRequest.of(1, 20, Sort.by("createdAt").descending());

        Page<Transaction> mockPage = new PageImpl<>(List.of(), pageable, 0);

        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
                .thenReturn(mockPage);

        Page<Transaction> result = transactionService.getTransactionsByUserId(userId, pageable);

        assertThat(result).isSameAs(mockPage);
        verify(transactionRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Test
    void isEventTransactionExists_shouldCallRepository() {
        String eventId = "evt-check";

        when(transactionRepository.existsByEventId(eventId)).thenReturn(true);

        boolean exists = transactionService.isEventTransactionExists(eventId);

        assertThat(exists).isTrue();
        verify(transactionRepository).existsByEventId(eventId);
    }
}
