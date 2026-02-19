package ru.misis.gamification.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.misis.gamification.dto.admin.response.TransactionItemDto;
import ru.misis.gamification.dto.admin.response.TransactionPageDto;
import ru.misis.gamification.model.admin.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionMapperTest {

    private final TransactionMapper mapper = Mappers.getMapper(TransactionMapper.class);

    @Test
    void toDto_shouldMapAllFields() {
        Transaction entity = Transaction.builder()
                .uuid(UUID.randomUUID())
                .userId("lms-user-123")
                .eventId("event-abc-001")
                .pointsEarned(250)
                .description("Выполнено задание #5")
                .createdAt(LocalDateTime.of(2026, 2, 15, 14, 30))
                .build();

        TransactionItemDto dto = mapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(entity.getUuid());
        assertThat(dto.getUserId()).isEqualTo("lms-user-123");
        assertThat(dto.getEventId()).isEqualTo("event-abc-001");
        assertThat(dto.getPointsEarned()).isEqualTo(250);
        assertThat(dto.getDescription()).isEqualTo("Выполнено задание #5");
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 2, 15, 14, 30));
    }

    @Test
    void toPageDto_shouldMapPageCorrectly() {
        Transaction t1 = Transaction.builder()
                .uuid(UUID.randomUUID())
                .userId("user-1")
                .eventId("e1")
                .pointsEarned(100)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        Transaction t2 = Transaction.builder()
                .uuid(UUID.randomUUID())
                .userId("user-2")
                .eventId("e2")
                .pointsEarned(200)
                .createdAt(LocalDateTime.now())
                .build();

        Page<Transaction> page = new PageImpl<>(
                List.of(t1, t2),
                PageRequest.of(0, 10),
                2
        );

        TransactionPageDto pageDto = mapper.toPageDto(page);

        assertThat(pageDto).isNotNull();
        assertThat(pageDto.getContent()).hasSize(2);
        assertThat(pageDto.getContent().get(0).getEventId()).isEqualTo("e1");
        assertThat(pageDto.getContent().get(1).getEventId()).isEqualTo("e2");
        assertThat(pageDto.getPageNumber()).isZero();
        assertThat(pageDto.getPageSize()).isEqualTo(10);
        assertThat(pageDto.getTotalElements()).isEqualTo(2);
        assertThat(pageDto.getTotalPages()).isEqualTo(1);
        assertThat(pageDto.isHasNext()).isFalse();
        assertThat(pageDto.isHasPrevious()).isFalse();
    }

    @Test
    void toPageDto_emptyPage_shouldReturnEmptyDto() {
        Page<Transaction> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        TransactionPageDto pageDto = mapper.toPageDto(emptyPage);

        assertThat(pageDto.getContent()).isEmpty();
        assertThat(pageDto.getTotalElements()).isZero();
        assertThat(pageDto.getTotalPages()).isZero();
        assertThat(pageDto.isHasNext()).isFalse();
        assertThat(pageDto.isHasPrevious()).isFalse();
    }

    @Test
    void toDto_nullInput_shouldReturnNull() {
        TransactionItemDto dto = mapper.toDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void toPageDto_nullPage_shouldReturnEmptyPage() {
        TransactionPageDto pageDto = mapper.toPageDto(null);

        assertThat(pageDto.getContent()).isEmpty();
        assertThat(pageDto.getPageNumber()).isZero();
        assertThat(pageDto.getPageSize()).isZero();
        assertThat(pageDto.getTotalElements()).isZero();
        assertThat(pageDto.getTotalPages()).isZero();
        assertThat(pageDto.isHasNext()).isFalse();
        assertThat(pageDto.isHasPrevious()).isFalse();
    }
}