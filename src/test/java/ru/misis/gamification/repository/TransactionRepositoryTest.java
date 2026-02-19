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
import ru.misis.gamification.model.admin.EventType;
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

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Test
    void existsByEventId_existingEventId_shouldReturnTrue() {
        // 1. Создаём тип события (обязательно!)
        EventType type = EventType.builder()
                .typeCode("test-type")
                .displayName("Тестовый тип")  // обязательно для @NotBlank
                .points(100)
                .active(true)
                .build();
        eventTypeRepository.save(type);

        // 2. Создаём транзакцию с существующим type_code
        Transaction transaction = Transaction.builder()
                .userId("lms-user-123")
                .eventId("event-unique-001")
                .eventTypeCode("test-type")   // ← теперь существует в event_types
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

        LocalDateTime base = LocalDateTime.of(2026, 2, 18, 12, 0, 0);

        // 1. Создаём типы событий заранее
        EventType type1 = EventType.builder()
                .typeCode("type1")
                .displayName("Тип 1")
                .points(50)
                .active(true)
                .build();
        eventTypeRepository.save(type1);

        EventType type2 = EventType.builder()
                .typeCode("type2")
                .displayName("Тип 2")
                .points(100)
                .active(true)
                .build();
        eventTypeRepository.save(type2);

        EventType type3 = EventType.builder()
                .typeCode("type3")
                .displayName("Тип 3")
                .points(200)
                .active(true)
                .build();
        eventTypeRepository.save(type3);

        // 2. Создаём транзакции с существующими type_code
        Transaction t1 = Transaction.builder()
                .userId(userId)
                .eventId("e1-" + UUID.randomUUID().toString().substring(0, 8))
                .eventTypeCode("type1")
                .pointsEarned(50)
                .createdAt(base.minusHours(48))
                .build();

        Transaction t2 = Transaction.builder()
                .userId(userId)
                .eventId("e2-" + UUID.randomUUID().toString().substring(0, 8))
                .eventTypeCode("type2")
                .pointsEarned(100)
                .createdAt(base.minusHours(24))
                .build();

        Transaction t3 = Transaction.builder()
                .userId("other-user")
                .eventId("e3-" + UUID.randomUUID().toString().substring(0, 8))
                .eventTypeCode("type3")
                .pointsEarned(200)
                .createdAt(base)
                .build();

        transactionRepository.saveAll(List.of(t1, t2, t3));

        // Для отладки — выведем реальные сохранённые даты
        System.out.println("После сохранения:");
        transactionRepository.findAll().forEach(tx ->
                System.out.println(tx.getEventId() + " → " + tx.getCreatedAt())
        );

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