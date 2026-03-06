package ru.misis.gamification.service.application.transaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.entity.Transaction;
import ru.misis.gamification.mapper.TransactionMapper;
import ru.misis.gamification.model.TransactionSummary;
import ru.misis.gamification.service.simple.transaction.TransactionService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionHistoryApplicationServiceUnitTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionHistoryApplicationServiceImpl service;

    @Test
    void getTransactionsByUserId_mapsPage() {
        String userId = "user-123";
        Pageable pageable = PageRequest.of(0, 20);

        UUID txId1 = UUID.randomUUID();
        UUID txId2 = UUID.randomUUID();

        Transaction t1 = Transaction.builder().uuid(txId1).points(50).build();
        Transaction t2 = Transaction.builder().uuid(txId2).points(100).build();

        Page<Transaction> entityPage = new PageImpl<>(List.of(t1, t2), pageable, 2);

        TransactionSummary s1 = new TransactionSummary(txId1, userId, "evt1", 50, "desc1", LocalDateTime.now());
        TransactionSummary s2 = new TransactionSummary(txId2, userId, "evt2", 100, "desc2", LocalDateTime.now());

        when(transactionService.getTransactionsByUserId(userId, pageable)).thenReturn(entityPage);
        when(transactionMapper.toTransactionSummary(t1)).thenReturn(s1);
        when(transactionMapper.toTransactionSummary(t2)).thenReturn(s2);

        Page<TransactionSummary> result = service.getTransactionsByUserId(userId, pageable);

        assertThat(result.getContent()).containsExactly(s1, s2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(20);

        verify(transactionService).getTransactionsByUserId(userId, pageable);
        verify(transactionMapper).toTransactionSummary(t1);
        verify(transactionMapper).toTransactionSummary(t2);
    }

    @Test
    void getTransactionsByUserId_emptyPage_returnsEmptyMappedPage() {
        String userId = "user-empty";
        Pageable pageable = PageRequest.of(1, 10);

        Page<Transaction> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(transactionService.getTransactionsByUserId(userId, pageable)).thenReturn(emptyPage);

        Page<TransactionSummary> result = service.getTransactionsByUserId(userId, pageable);

        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getNumber()).isOne();
        assertThat(result.getSize()).isEqualTo(10);

        verify(transactionService).getTransactionsByUserId(userId, pageable);
        verifyNoInteractions(transactionMapper);
    }

    @Test
    void getTransactionsByUserId_nullUserId_callsServiceWithNull() {
        Pageable pageable = PageRequest.of(0, 5);

        Page<Transaction> page = new PageImpl<>(Collections.emptyList());
        when(transactionService.getTransactionsByUserId(null, pageable)).thenReturn(page);

        Page<TransactionSummary> result = service.getTransactionsByUserId(null, pageable);

        assertThat(result.isEmpty()).isTrue();
        verify(transactionService).getTransactionsByUserId(null, pageable);
    }

    @Test
    void getTransactionsByUserId_serviceThrowsException_propagates() {
        String userId = "user-err";
        Pageable pageable = PageRequest.of(0, 10);

        RuntimeException ex = new RuntimeException("DB error");
        when(transactionService.getTransactionsByUserId(userId, pageable)).thenThrow(ex);

        assertThatThrownBy(() -> service.getTransactionsByUserId(userId, pageable))
                .isSameAs(ex);

        verify(transactionService).getTransactionsByUserId(userId, pageable);
        verifyNoInteractions(transactionMapper);
    }
}