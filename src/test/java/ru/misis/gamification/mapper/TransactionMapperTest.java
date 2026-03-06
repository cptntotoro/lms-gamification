package ru.misis.gamification.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.misis.gamification.dto.admin.response.TransactionItemDto;
import ru.misis.gamification.dto.admin.response.TransactionPageDto;
import ru.misis.gamification.entity.Transaction;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.model.TransactionSummary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionMapperTest {

    private final TransactionMapper mapper = Mappers.getMapper(TransactionMapper.class);

    @Test
    void toTransactionItemDto_mapsAllFieldsCorrectly() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        TransactionSummary summary = new TransactionSummary(
                uuid,
                "user-abc123",
                "event-xyz789",
                150,
                "Прохождение теста #5",
                now
        );

        TransactionItemDto dto = mapper.toTransactionItemDto(summary);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getUserId()).isEqualTo("user-abc123");
        assertThat(dto.getEventId()).isEqualTo("event-xyz789");
        assertThat(dto.getPoints()).isEqualTo(150);
        assertThat(dto.getDescription()).isEqualTo("Прохождение теста #5");
        assertThat(dto.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toTransactionItemDto_nullSummary_returnsNull() {
        assertThat(mapper.toTransactionItemDto(null)).isNull();
    }

    @Test
    void toTransactionSummary_mapsAllFieldsCorrectly() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .uuid(UUID.randomUUID())
                .userId("student-456")
                .build();

        Transaction entity = Transaction.builder()
                .uuid(uuid)
                .user(user)
                .eventId("quiz-event-001")
                .points(200)
                .description("Успешный квиз")
                .createdAt(now)
                .build();

        TransactionSummary summary = mapper.toTransactionSummary(entity);

        assertThat(summary).isNotNull();
        assertThat(summary.uuid()).isEqualTo(uuid);
        assertThat(summary.userId()).isEqualTo("student-456");
        assertThat(summary.eventId()).isEqualTo("quiz-event-001");
        assertThat(summary.points()).isEqualTo(200);
        assertThat(summary.description()).isEqualTo("Успешный квиз");
        assertThat(summary.createdAt()).isEqualTo(now);
    }

    @Test
    void toTransactionSummary_nullTransaction_returnsNull() {
        assertThat(mapper.toTransactionSummary(null)).isNull();
    }

    @Test
    void toTransactionPageDto_mapsPageCorrectly() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        List<TransactionSummary> summaries = List.of(
                new TransactionSummary(uuid1, "alice", "event-1", 100, "Тест 1", now.minusHours(2)),
                new TransactionSummary(uuid2, "bob", "event-2", 50, "Квиз 2", now.minusHours(1))
        );

        Page<TransactionSummary> page = new PageImpl<>(
                summaries,
                PageRequest.of(0, 10),
                25L  // всего 25 элементов
        );

        TransactionPageDto dto = mapper.toTransactionPageDto(page);

        assertThat(dto).isNotNull();
        assertThat(dto.getContent()).hasSize(2);
        assertThat(dto.getContent().get(0).getUuid()).isEqualTo(uuid1);
        assertThat(dto.getContent().get(0).getUserId()).isEqualTo("alice");
        assertThat(dto.getContent().get(1).getPoints()).isEqualTo(50);

        // Пагинация
        assertThat(dto.getPageNumber()).isEqualTo(0);
        assertThat(dto.getPageSize()).isEqualTo(10);
        assertThat(dto.getTotalElements()).isEqualTo(25L);
        assertThat(dto.getTotalPages()).isEqualTo(3);  // 25 / 10 = 3 страницы
        assertThat(dto.isHasNext()).isTrue();
        assertThat(dto.isHasPrevious()).isFalse();
    }

    @Test
    void toTransactionPageDto_emptyPage_mapsCorrectly() {
        Page<TransactionSummary> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(1, 20),
                0L
        );

        TransactionPageDto dto = mapper.toTransactionPageDto(emptyPage);

        assertThat(dto.getContent()).isEmpty();
        assertThat(dto.getPageNumber()).isEqualTo(1);
        assertThat(dto.getPageSize()).isEqualTo(20);
        assertThat(dto.getTotalElements()).isZero();
        assertThat(dto.getTotalPages()).isZero();
        assertThat(dto.isHasNext()).isFalse();
        assertThat(dto.isHasPrevious()).isTrue();
    }

    @Test
    void toTransactionPageDto_nullPage_returnsNull() {
        assertThat(mapper.toTransactionPageDto(null)).isNull();
    }
}