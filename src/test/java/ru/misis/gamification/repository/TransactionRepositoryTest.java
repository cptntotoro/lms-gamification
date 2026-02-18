package ru.misis.gamification.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import ru.misis.gamification.model.admin.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void existsByEventId_existingEventId_shouldReturnTrue() {
        Transaction transaction = Transaction.builder()
                .userId("lms-user-123")
                .eventId("event-unique-001")
                .pointsEarned(100)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        boolean exists = transactionRepository.existsByEventId("event-unique-001");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEventId_nonExistingEventId_shouldReturnFalse() {
        boolean exists = transactionRepository.existsByEventId("non-existing-event");

        assertThat(exists).isFalse();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_shouldReturnSortedPage() {
        String userId = "lms-user-abc";

        // Фиксированная базовая дата — чтобы избежать миллисекундных гонок
        LocalDateTime baseTime = LocalDateTime.of(2026, 2, 18, 12, 0, 0);

        Transaction t1 = Transaction.builder()
                .userId(userId)
                .eventId("e1-" + UUID.randomUUID().toString().substring(0, 8))
                .pointsEarned(50)
                .createdAt(baseTime.minusDays(2))
                .build();

        Transaction t2 = Transaction.builder()
                .userId(userId)
                .eventId("e2-" + UUID.randomUUID().toString().substring(0, 8))
                .pointsEarned(100)
                .createdAt(baseTime.minusDays(1))
                .build();

        Transaction t3 = Transaction.builder()
                .userId("other-user")
                .eventId("e3-" + UUID.randomUUID().toString().substring(0, 8))
                .pointsEarned(200)
                .createdAt(baseTime)
                .build();

        transactionRepository.saveAll(List.of(t1, t2, t3));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Transaction> page = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);

        assertThat(page.getContent().get(0).getEventId()).startsWith("e2");
        assertThat(page.getContent().get(1).getEventId()).startsWith("e1");
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_nonExistingUser_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Transaction> page = transactionRepository.findByUserIdOrderByCreatedAtDesc("unknown-user", pageable);

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }
}