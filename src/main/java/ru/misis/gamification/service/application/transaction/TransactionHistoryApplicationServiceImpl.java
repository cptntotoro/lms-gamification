package ru.misis.gamification.service.application.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.Transaction;
import ru.misis.gamification.model.TransactionSummary;
import ru.misis.gamification.service.simple.transaction.TransactionService;
import ru.misis.gamification.service.simple.user.UserService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Validated
public class TransactionHistoryApplicationServiceImpl implements TransactionHistoryApplicationService {
    private final TransactionService transactionService;
    private final UserService userService;

    @Transactional(readOnly = true)
    @Override
    public Page<TransactionSummary> getTransactionsByUserId(String userId, Pageable pageable) {
        Page<Transaction> entityPage = transactionService.getTransactionsByUserId(userId, pageable);
        return entityPage.map(this::toSummary);
    }

    private TransactionSummary toSummary(Transaction tx) {
        return new TransactionSummary(
                tx.getUuid(),
                tx.getUser().getUserId(),
                tx.getEventId(),
                tx.getPoints(),
                tx.getDescription(),
                tx.getCreatedAt()
        );
    }
}
