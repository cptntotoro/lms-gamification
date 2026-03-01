package ru.misis.gamification.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.entity.Transaction;
import ru.misis.gamification.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventTypeRepositoryTest {

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByTypeCodeAndActiveTrue_existingActive_returnsType() {
        EventType saved = eventTypeRepository.save(EventType.builder()
                .typeCode("quiz")
                .displayName("Квиз")
                .points(50)
                .active(true)
                .build());

        Optional<EventType> found = eventTypeRepository.findByTypeCodeAndActiveTrue("quiz");

        assertThat(found).isPresent();
        assertThat(found.get().getUuid()).isEqualTo(saved.getUuid());
        assertThat(found.get().getTypeCode()).isEqualTo("quiz");
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    void findByTypeCodeAndActiveTrue_inactive_returnsEmpty() {
        eventTypeRepository.save(EventType.builder()
                .typeCode("lab")
                .displayName("Лабораторная")
                .points(150)
                .active(false)
                .build());

        assertThat(eventTypeRepository.findByTypeCodeAndActiveTrue("lab")).isEmpty();
    }

    @Test
    void findByTypeCodeAndActiveTrue_nonExisting_returnsEmpty() {
        assertThat(eventTypeRepository.findByTypeCodeAndActiveTrue("unknown")).isEmpty();
    }

    @Test
    void findByTypeCodeAndActiveTrue_nullTypeCode_returnsEmpty() {
        assertThat(eventTypeRepository.findByTypeCodeAndActiveTrue(null)).isEmpty();
    }

    @Test
    void findByTypeCodeAndActiveTrue_emptyTypeCode_returnsEmpty() {
        assertThat(eventTypeRepository.findByTypeCodeAndActiveTrue("")).isEmpty();
    }

    @Test
    void findByTypeCodeAndActiveTrue_caseSensitive() {
        eventTypeRepository.save(EventType.builder().typeCode("Quiz").displayName("Верхний").points(10).active(true).build());
        eventTypeRepository.save(EventType.builder().typeCode("quiz").displayName("Нижний").points(20).active(true).build());

        assertThat(eventTypeRepository.findByTypeCodeAndActiveTrue("Quiz").get().getDisplayName()).isEqualTo("Верхний");
        assertThat(eventTypeRepository.findByTypeCodeAndActiveTrue("quiz").get().getDisplayName()).isEqualTo("Нижний");
        assertThat(eventTypeRepository.findByTypeCodeAndActiveTrue("QUIZ")).isEmpty();
    }

    @Test
    void existsByTypeCode_existing_returnsTrue() {
        eventTypeRepository.save(EventType.builder().typeCode("homework").displayName("ДЗ").points(100).active(true).build());
        assertThat(eventTypeRepository.existsByTypeCode("homework")).isTrue();
    }

    @Test
    void existsByTypeCode_nonExisting_returnsFalse() {
        assertThat(eventTypeRepository.existsByTypeCode("missing")).isFalse();
    }

    @Test
    void existsByTypeCode_null_returnsFalse() {
        assertThat(eventTypeRepository.existsByTypeCode(null)).isFalse();
    }

    @Test
    void existsByTypeCode_empty_returnsFalse() {
        assertThat(eventTypeRepository.existsByTypeCode("")).isFalse();
    }

    @Test
    void existsByTypeCode_caseSensitive() {
        eventTypeRepository.save(EventType.builder().typeCode("TEST").displayName("Тест").points(50).active(true).build());
        assertThat(eventTypeRepository.existsByTypeCode("TEST")).isTrue();
        assertThat(eventTypeRepository.existsByTypeCode("test")).isFalse();
    }

    @Test
    void calculateDailyPointsSumForUserAndType_correctSum() {
        User user = userRepository.save(User.builder().userId("u-001").totalPoints(0).level(1).build());

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
                // сегодня quiz
                Transaction.builder().user(user).eventId("q1").eventType(quiz).points(50).createdAt(today.atStartOfDay().plusHours(9)).build(),
                Transaction.builder().user(user).eventId("q2").eventType(quiz).points(70).createdAt(today.atStartOfDay().plusHours(12)).build(),
                // сегодня hw
                Transaction.builder().user(user).eventId("h1").eventType(hw).points(100).createdAt(today.atStartOfDay().plusHours(15)).build(),
                // вчера quiz
                Transaction.builder().user(user).eventId("qy").eventType(quiz).points(200).createdAt(yesterday.atStartOfDay().plusHours(10)).build()
        ));

        long quizToday = eventTypeRepository.calculateDailyPointsSumForUserAndType(user.getUuid(), quiz.getUuid(), today);
        long hwToday = eventTypeRepository.calculateDailyPointsSumForUserAndType(user.getUuid(), hw.getUuid(), today);
        long quizYest = eventTypeRepository.calculateDailyPointsSumForUserAndType(user.getUuid(), quiz.getUuid(), yesterday);

        assertThat(quizToday).isEqualTo(120);   // 50 + 70
        assertThat(hwToday).isEqualTo(100);
        assertThat(quizYest).isEqualTo(200);
    }

    @Test
    void calculateDailyPointsSumForUserAndType_noTransactions_returnsZero() {
        User user = userRepository.save(User.builder().userId("u-empty").totalPoints(0).level(1).build());
        EventType type = eventTypeRepository.save(EventType.builder().typeCode("empty").displayName("Пустой").points(10).active(true).build());

        long sum = eventTypeRepository.calculateDailyPointsSumForUserAndType(
                user.getUuid(), type.getUuid(), LocalDate.now()
        );

        assertThat(sum).isZero();
    }

    @Test
    void calculateDailyPointsSumForUserAndType_wrongTypeOrDate_returnsZero() {
        User user = userRepository.save(User.builder().userId("u-002").totalPoints(0).level(1).build());
        EventType type = eventTypeRepository.save(EventType.builder().typeCode("typeA").displayName("Тип A").points(30).active(true).build());

        transactionRepository.save(Transaction.builder()
                .user(user)
                .eventId("t1")
                .eventType(type)
                .points(30)
                .createdAt(LocalDate.now().atStartOfDay())
                .build());

        long wrongType = eventTypeRepository.calculateDailyPointsSumForUserAndType(
                user.getUuid(), UUID.randomUUID(), LocalDate.now()
        );

        long wrongDate = eventTypeRepository.calculateDailyPointsSumForUserAndType(
                user.getUuid(), type.getUuid(), LocalDate.now().minusDays(5)
        );

        assertThat(wrongType).isZero();
        assertThat(wrongDate).isZero();
    }
}