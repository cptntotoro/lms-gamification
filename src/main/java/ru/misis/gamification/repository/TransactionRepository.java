package ru.misis.gamification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.misis.gamification.model.admin.Transaction;

/**
 * Репозиторий транзакций
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Проверить наличие транзакций по идентификатору события из LMS
     *
     * @param eventId Идентификатор события из LMS
     * @return Да / Нет
     */
    boolean existsByEventId(String eventId);

    /**
     * Получить страницу транзакций по идентификатору пользователя из LMS
     *
     * @param userId   Идентификатор пользователя из LMS
     * @param pageable Параметры пагинации и сортировки
     * @return Страница транзакций
     */
    Page<Transaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}