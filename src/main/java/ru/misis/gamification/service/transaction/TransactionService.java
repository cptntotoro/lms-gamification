package ru.misis.gamification.service.transaction;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.exception.DuplicateEventException;
import ru.misis.gamification.model.admin.Transaction;

import java.time.LocalDate;

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
    boolean isExistsByEventId(String eventId);

    /**
     * Сохранить транзакцию, если событие с таким {@link Transaction#getEventId()} ещё не обрабатывалось
     *
     * @param transaction Транзакция
     * @return Транзакция
     * @throws DuplicateEventException         если событие уже существует
     * @throws DataIntegrityViolationException при нарушении уникальности
     */
    Transaction saveIfNotExists(Transaction transaction);

    /**
     * Получить страницу транзакции по идентификатору пользователя из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @param pageable Параметры пагинации и сортировки
     * @return Страница транзакций
     */
    Page<Transaction> getTransactionsByUserId(String userId, Pageable pageable);

    long sumPointsByUserIdAndEventTypeAndDate(String userId, String typeCode, LocalDate date);
}
