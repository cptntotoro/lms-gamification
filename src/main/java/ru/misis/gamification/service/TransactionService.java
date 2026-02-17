package ru.misis.gamification.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.exception.DuplicateEventException;
import ru.misis.gamification.model.admin.Transaction;

/**
 * Сервис управления транзакциями
 * Основная задача — гарантировать уникальность eventId
 */
public interface TransactionService {

    /**
     * Проверить, было ли событие с указанным идентификатором уже обработано
     *
     * @param eventId Идентификатор события из LMS
     * @return Да / Нет
     */
    boolean isEventTransactionExists(String eventId);

    /**
     * Сохранить транзакцию, если событие с таким eventId ещё не обрабатывалось.
     *
     * @param transaction Транзакция
     * @return Транзакция
     * @throws DuplicateEventException         если событие уже существует
     * @throws DataIntegrityViolationException при нарушении уникальности
     */
    Transaction saveIfNotExists(Transaction transaction) throws DuplicateEventException;

    Page<Transaction> getTransactionsByUserId(String userId, Pageable pageable);
}
