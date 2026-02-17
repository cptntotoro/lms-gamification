package ru.misis.gamification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.misis.gamification.model.admin.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий транзакций
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByEventId(String eventId);

    Page<Transaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Optional<Transaction> findByEventId(String eventId);

    List<Transaction> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findLatestByUserId(@Param("userId") String userId);

    Long countByUserId(String userId);

    @Query("SELECT SUM(t.pointsEarned) FROM Transaction t WHERE t.userId = :userId")
    Long sumPointsByUserId(@Param("userId") String userId);

    @Query("SELECT t FROM Transaction t WHERE t.createdAt >= :since")
    List<Transaction> findRecentTransactions(@Param("since") LocalDateTime since);
}