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
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.entity.Transaction;
import ru.misis.gamification.entity.User;

import java.time.LocalDate;
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

    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByEventId_existingEventId_shouldReturnTrue() {
        User user = userRepository.save(User.builder().userId("u-001").totalPoints(0).level(1).build());

        EventType type = eventTypeRepository.save(EventType.builder()
                .typeCode("test-type")
                .displayName("Тестовый тип")
                .points(100)
                .active(true)
                .build());

        transactionRepository.save(Transaction.builder()
                .user(user)
                .eventId("evt-unique-001")
                .eventType(type)
                .points(100)
                .createdAt(LocalDateTime.now())
                .build());

        boolean exists = transactionRepository.existsByEventId("evt-unique-001");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEventId_nonExistingEventId_shouldReturnFalse() {
        boolean exists = transactionRepository.existsByEventId("non-existing-evt");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByEventId_nullEventId_shouldReturnFalse() {
        boolean exists = transactionRepository.existsByEventId(null);

        assertThat(exists).isFalse();
    }

    @Test
    void existsByEventId_emptyEventId_shouldReturnFalse() {
        boolean exists = transactionRepository.existsByEventId("");

        assertThat(exists).isFalse();
    }

    @Test
    void findByUserUuidOrderByCreatedAtDesc_shouldReturnSortedPage() {
        User user = userRepository.save(User.builder().userId("u-abc").totalPoints(0).level(1).build());

        EventType type1 = eventTypeRepository.save(EventType.builder()
                .typeCode("t1")
                .displayName("Тип 1")
                .points(50)
                .active(true)
                .build());

        EventType type2 = eventTypeRepository.save(EventType.builder()
                .typeCode("t2")
                .displayName("Тип 2")
                .points(100)
                .active(true)
                .build());

        LocalDateTime base = LocalDateTime.of(2026, 3, 1, 12, 0);

        Transaction t1 = Transaction.builder()
                .user(user)
                .eventId("e1")
                .eventType(type1)
                .points(50)
                .createdAt(base.minusHours(48))
                .build();

        Transaction t2 = Transaction.builder()
                .user(user)
                .eventId("e2")
                .eventType(type2)
                .points(100)
                .createdAt(base.minusHours(24))
                .build();

        Transaction t3 = Transaction.builder()
                .user(userRepository.save(User.builder().userId("other").totalPoints(0).level(1).build()))
                .eventId("e3")
                .eventType(type1)
                .points(200)
                .createdAt(base)
                .build();

        transactionRepository.saveAll(List.of(t1, t2, t3));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Transaction> page = transactionRepository.findByUserUuidOrderByCreatedAtDesc(user.getUuid(), pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getEventId()).isEqualTo("e2");
        assertThat(page.getContent().get(1).getEventId()).isEqualTo("e1");
    }

    @Test
    void findByUserUuidOrderByCreatedAtDesc_nonExistingUser_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Transaction> page = transactionRepository.findByUserUuidOrderByCreatedAtDesc(
                UUID.randomUUID(), pageable
        );

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findByUserUuidOrderByCreatedAtDesc_nullUserUuid_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Transaction> page = transactionRepository.findByUserUuidOrderByCreatedAtDesc(null, pageable);

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void sumPointsByUserUuidAndEventTypeUuidAndDate_correctSum() {
        User user = userRepository.save(User.builder().userId("u-sum").totalPoints(0).level(1).build());

        EventType quiz = eventTypeRepository.save(EventType.builder()
                .typeCode("quiz")
                .displayName("Квиз")
                .points(50)
                .active(true)
                .build());

        EventType hw = eventTypeRepository.save(EventType.builder()
                .typeCode("hw")
                .displayName("Домашка")
                .points(100)
                .active(true)
                .build());

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        transactionRepository.saveAll(List.of(
                Transaction.builder().user(user).eventId("q1").eventType(quiz).points(50).createdAt(today.atStartOfDay().plusHours(9)).build(),
                Transaction.builder().user(user).eventId("q2").eventType(quiz).points(70).createdAt(today.atStartOfDay().plusHours(12)).build(),
                Transaction.builder().user(user).eventId("h1").eventType(hw).points(100).createdAt(today.atStartOfDay().plusHours(15)).build(),
                Transaction.builder().user(user).eventId("qy").eventType(quiz).points(200).createdAt(yesterday.atStartOfDay().plusHours(10)).build()
        ));

        long quizToday = transactionRepository.sumPointsByUserUuidAndEventTypeUuidAndDate(
                user.getUuid(), quiz.getUuid(), today
        );

        long hwToday = transactionRepository.sumPointsByUserUuidAndEventTypeUuidAndDate(
                user.getUuid(), hw.getUuid(), today
        );

        long quizYesterday = transactionRepository.sumPointsByUserUuidAndEventTypeUuidAndDate(
                user.getUuid(), quiz.getUuid(), yesterday
        );

        assertThat(quizToday).isEqualTo(120);
        assertThat(hwToday).isEqualTo(100);
        assertThat(quizYesterday).isEqualTo(200);
    }

    @Test
    void sumPointsByUserUuidAndEventTypeUuidAndDate_noRecords_returnsZero() {
        long sum = transactionRepository.sumPointsByUserUuidAndEventTypeUuidAndDate(
                UUID.randomUUID(), UUID.randomUUID(), LocalDate.now()
        );

        assertThat(sum).isZero();
    }

    @Test
    void sumPointsByUserUuidAndEventTypeUuidAndDate_wrongTypeOrDate_returnsZero() {
        User user = userRepository.save(User.builder().userId("u-wrong").totalPoints(0).level(1).build());

        EventType type = eventTypeRepository.save(EventType.builder()
                .typeCode("type")
                .displayName("Тип")
                .points(30)
                .active(true)
                .build());

        transactionRepository.save(Transaction.builder()
                .user(user)
                .eventId("t1")
                .eventType(type)
                .points(30)
                .createdAt(LocalDate.now().atStartOfDay())
                .build());

        long wrongType = transactionRepository.sumPointsByUserUuidAndEventTypeUuidAndDate(
                user.getUuid(), UUID.randomUUID(), LocalDate.now()
        );

        long wrongDate = transactionRepository.sumPointsByUserUuidAndEventTypeUuidAndDate(
                user.getUuid(), type.getUuid(), LocalDate.now().minusDays(10)
        );

        assertThat(wrongType).isZero();
        assertThat(wrongDate).isZero();
    }
}