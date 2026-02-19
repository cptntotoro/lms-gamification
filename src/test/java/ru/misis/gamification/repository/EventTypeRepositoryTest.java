package ru.misis.gamification.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.model.admin.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventTypeRepositoryTest {

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void findByTypeCodeAndActiveTrue_existingActive_returnsType() {
        EventType type = EventType.builder()
                .typeCode("quiz")
                .displayName("Тест")
                .points(100)
                .active(true)
                .build();

        eventTypeRepository.save(type);

        Optional<EventType> found = eventTypeRepository.findByTypeCodeAndActiveTrue("quiz");

        assertThat(found).isPresent();
        assertThat(found.get().getTypeCode()).isEqualTo("quiz");
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    void findByTypeCodeAndActiveTrue_inactive_returnsEmpty() {
        EventType type = EventType.builder()
                .typeCode("lab")
                .displayName("Лаба")
                .points(150)
                .active(false)
                .build();

        eventTypeRepository.save(type);

        Optional<EventType> found = eventTypeRepository.findByTypeCodeAndActiveTrue("lab");

        assertThat(found).isEmpty();
    }

    @Test
    void findByTypeCodeAndActiveTrue_nonExisting_returnsEmpty() {
        Optional<EventType> found = eventTypeRepository.findByTypeCodeAndActiveTrue("unknown");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByTypeCode_existing_returnsTrue() {
        EventType type = EventType.builder()
                .typeCode("homework")
                .displayName("Домашка")
                .points(60)
                .active(true)
                .build();

        eventTypeRepository.save(type);

        boolean exists = eventTypeRepository.existsByTypeCode("homework");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByTypeCode_nonExisting_returnsFalse() {
        boolean exists = eventTypeRepository.existsByTypeCode("nonexistent");

        assertThat(exists).isFalse();
    }

    @Test
    void sumPointsByUserIdAndEventTypeAndDate_calculatesCorrectSum() {
        EventType quizType = EventType.builder()
                .typeCode("quiz")
                .displayName("Квиз / Тест")
                .points(80)
                .active(true)
                .build();
        eventTypeRepository.save(quizType);

        EventType labType = EventType.builder()
                .typeCode("lab")
                .displayName("Лабораторная работа")
                .points(150)
                .active(true)
                .build();
        eventTypeRepository.save(labType);

        String userId = "user-123";

        LocalDate today = LocalDate.now();

        // Сохраняем транзакции с существующими type_code
        transactionRepository.saveAll(List.of(
                Transaction.builder()
                        .userId(userId)
                        .eventId("evt-q1")
                        .eventTypeCode("quiz")
                        .pointsEarned(80)
                        .createdAt(today.atStartOfDay().plusHours(10))
                        .build(),
                Transaction.builder()
                        .userId(userId)
                        .eventId("evt-q2")
                        .eventTypeCode("quiz")
                        .pointsEarned(120)
                        .createdAt(today.atStartOfDay().plusHours(12))
                        .build(),
                Transaction.builder()
                        .userId(userId)
                        .eventId("evt-l1")
                        .eventTypeCode("lab")
                        .pointsEarned(150)
                        .createdAt(today.atStartOfDay().plusHours(14))
                        .build(),
                Transaction.builder()
                        .userId(userId)
                        .eventId("evt-q-yesterday")
                        .eventTypeCode("quiz")
                        .pointsEarned(200)
                        .createdAt(today.minusDays(1).atStartOfDay().plusHours(10))
                        .build()
        ));

        long quizToday = eventTypeRepository.sumPointsByUserIdAndEventTypeAndDate(userId, "quiz", today);
        long labToday = eventTypeRepository.sumPointsByUserIdAndEventTypeAndDate(userId, "lab", today);
        long quizYesterday = eventTypeRepository.sumPointsByUserIdAndEventTypeAndDate(userId, "quiz", today.minusDays(1));

        assertThat(quizToday).isEqualTo(200);      // 80 + 120
        assertThat(labToday).isEqualTo(150);
        assertThat(quizYesterday).isEqualTo(200);
    }

    @Test
    void sumPointsByUserIdAndEventTypeAndDate_noRecords_returnsZero() {
        long sum = eventTypeRepository.sumPointsByUserIdAndEventTypeAndDate(
                "unknown-user",
                "quiz",
                LocalDate.now()
        );

        assertThat(sum).isZero();
    }

    @Test
    void sumPointsByUserIdAndEventTypeAndDate_filtersByTypeAndDate() {
        // Создаём все используемые типы событий
        EventType testType = EventType.builder()
                .typeCode("test")
                .displayName("Тестовый тип")
                .points(50)
                .active(true)
                .build();
        eventTypeRepository.save(testType);

        EventType otherType = EventType.builder()
                .typeCode("other")
                .displayName("Другой тип")  // ← обязательно добавляем
                .points(1000)
                .active(true)
                .build();
        eventTypeRepository.save(otherType);

        String userId = "user-filter";

        LocalDate today = LocalDate.now();

        transactionRepository.saveAll(List.of(
                Transaction.builder()
                        .userId(userId)
                        .eventId("evt-1")
                        .eventTypeCode("test")
                        .pointsEarned(50)
                        .createdAt(today.atStartOfDay())
                        .build(),
                Transaction.builder()
                        .userId(userId)
                        .eventId("evt-2")
                        .eventTypeCode("other")  // ← теперь существует
                        .pointsEarned(1000)
                        .createdAt(today.atStartOfDay())
                        .build(),
                Transaction.builder()
                        .userId(userId)
                        .eventId("evt-3")
                        .eventTypeCode("test")
                        .pointsEarned(50)
                        .createdAt(today.minusDays(1).atStartOfDay())
                        .build()
        ));

        long todaySum = eventTypeRepository.sumPointsByUserIdAndEventTypeAndDate(userId, "test", today);
        long yesterdaySum = eventTypeRepository.sumPointsByUserIdAndEventTypeAndDate(userId, "test", today.minusDays(1));

        assertThat(todaySum).isEqualTo(50);
        assertThat(yesterdaySum).isEqualTo(50);
    }
}