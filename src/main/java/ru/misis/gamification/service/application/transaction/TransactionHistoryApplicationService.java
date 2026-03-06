package ru.misis.gamification.service.application.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.model.TransactionSummary;

/**
 * Фасадный сервис управления историей транзакций
 */
public interface TransactionHistoryApplicationService {

    /**
     * Получить страницу транзакций пользователя по его идентификатору из LMS
     *
     * @param userId   Идентификатор пользователя из LMS
     * @param pageable Параметры пагинации и сортировки
     * @return Cтраница транзакций пользователя
     */
    Page<TransactionSummary> getTransactionsByUserId(String userId, Pageable pageable);
}
