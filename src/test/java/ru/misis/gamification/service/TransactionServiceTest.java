package ru.misis.gamification.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.exception.DuplicateEventException;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.repository.TransactionRepository;
import ru.misis.gamification.service.transaction.TransactionServiceImpl;

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
}
