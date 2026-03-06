package ru.misis.gamification.service.application.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.Transaction;
import ru.misis.gamification.mapper.TransactionMapper;
import ru.misis.gamification.model.TransactionSummary;
import ru.misis.gamification.service.simple.transaction.TransactionService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Validated
public class TransactionHistoryApplicationServiceImpl implements TransactionHistoryApplicationService {

    /**
     * Сервис управления транзакциями
     */
    private final TransactionService transactionService;

    /**
     * Маппер транзакций
     */
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    @Override
    public Page<TransactionSummary> getTransactionsByUserId(String userId, Pageable pageable) {
        Page<Transaction> entityPage = transactionService.getTransactionsByUserId(userId, pageable);
        return entityPage.map(transactionMapper::toTransactionSummary);
    }
}
