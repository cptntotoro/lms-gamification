package ru.misis.gamification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.misis.gamification.entity.Transaction;

import java.time.LocalDate;
import java.util.UUID;

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
     * @param userUuid UUID пользователя
     * @param pageable Параметры пагинации и сортировки
     * @return Страница транзакций
     */
    Page<Transaction> findByUserUuidOrderByCreatedAtDesc(UUID userUuid, Pageable pageable);

    /**
     * Получить сумму очков, начисленных пользователю по конкретному типу события за указанный день
     * <p>
     * Учитываются только транзакции, созданные в этот день (по дате без времени).
     * Если записей нет — возвращается 0.
     * </p>
     *
     * @param userUuid      UUID пользователя
     * @param eventTypeUuid UUID типа события
     * @param date          Дата (день), за который считается сумма
     * @return Сумма начисленных очков за день по этому типу события
     */
    @Query("SELECT COALESCE(SUM(t.points), 0) " +
            "FROM Transaction t " +
            "WHERE t.user.uuid = :userUuid " +
            "  AND t.eventType.uuid = :eventTypeUuid " +
            "  AND DATE(t.createdAt) = :date")
    long sumPointsByUserUuidAndEventTypeUuidAndDate(
            @Param("userUuid") UUID userUuid,
            @Param("eventTypeUuid") UUID eventTypeUuid,
            @Param("date") LocalDate date);
}