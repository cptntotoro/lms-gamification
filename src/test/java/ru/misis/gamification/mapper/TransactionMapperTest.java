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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionMapperTest {

    private final TransactionMapper mapper = Mappers.getMapper(TransactionMapper.class);

    @Test
    void transactionToTransactionItemDto_shouldMapAllFieldsCorrectly() {
        User user = User.builder()
                .uuid(UUID.randomUUID())
                .userId("lms-user-123")
                .build();

        Transaction entity = Transaction.builder()
                .uuid(UUID.randomUUID())
                .user(user)
                .eventId("event-abc-001")
                .points(250)
                .description("Выполнено задание #5")
                .createdAt(LocalDateTime.of(2026, 2, 15, 14, 30))
                .build();

        TransactionItemDto dto = mapper.transactionToTransactionItemDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(entity.getUuid());
        assertThat(dto.getUserId()).isEqualTo("lms-user-123");
        assertThat(dto.getEventId()).isEqualTo("event-abc-001");
        assertThat(dto.getPoints()).isEqualTo(250);
        assertThat(dto.getDescription()).isEqualTo("Выполнено задание #5");
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 2, 15, 14, 30));
    }

    @Test
    void transactionPagetoTransactionPageDto_shouldMapPageCorrectly() {
        User user1 = User.builder().userId("user-1").build();
        User user2 = User.builder().userId("user-2").build();

        Transaction t1 = Transaction.builder()
                .uuid(UUID.randomUUID())
                .user(user1)
                .eventId("e1")
                .points(100)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        Transaction t2 = Transaction.builder()
                .uuid(UUID.randomUUID())
                .user(user2)
                .eventId("e2")
                .points(200)
                .createdAt(LocalDateTime.now())
                .build();

        Page<Transaction> page = new PageImpl<>(
                List.of(t1, t2),
                PageRequest.of(0, 10),
                2
        );

        TransactionPageDto pageDto = mapper.transactionPagetoTransactionPageDto(page);

        assertThat(pageDto).isNotNull();
        assertThat(pageDto.getContent()).hasSize(2);
        assertThat(pageDto.getContent().get(0).getEventId()).isEqualTo("e1");
        assertThat(pageDto.getContent().get(1).getEventId()).isEqualTo("e2");
        assertThat(pageDto.getContent().get(0).getUserId()).isEqualTo("user-1");
        assertThat(pageDto.getContent().get(1).getUserId()).isEqualTo("user-2");
        assertThat(pageDto.getPageNumber()).isZero();
        assertThat(pageDto.getPageSize()).isEqualTo(10);
        assertThat(pageDto.getTotalElements()).isEqualTo(2L);
        assertThat(pageDto.getTotalPages()).isEqualTo(1);
        assertThat(pageDto.isHasNext()).isFalse();
        assertThat(pageDto.isHasPrevious()).isFalse();
    }

    @Test
    void transactionPagetoTransactionPageDto_emptyPage_shouldReturnEmptyDto() {
        Page<Transaction> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        TransactionPageDto pageDto = mapper.transactionPagetoTransactionPageDto(emptyPage);

        assertThat(pageDto.getContent()).isEmpty();
        assertThat(pageDto.getTotalElements()).isZero();
        assertThat(pageDto.getTotalPages()).isZero();
        assertThat(pageDto.isHasNext()).isFalse();
        assertThat(pageDto.isHasPrevious()).isFalse();
    }

    @Test
    void transactionToTransactionItemDto_nullInput_shouldReturnNull() {
        TransactionItemDto dto = mapper.transactionToTransactionItemDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void transactionPagetoTransactionPageDto_nullPage_shouldReturnEmptyPage() {
        TransactionPageDto pageDto = mapper.transactionPagetoTransactionPageDto(null);

        assertThat(pageDto.getContent()).isEmpty();
        assertThat(pageDto.getPageNumber()).isZero();
        assertThat(pageDto.getPageSize()).isZero();
        assertThat(pageDto.getTotalElements()).isZero();
        assertThat(pageDto.getTotalPages()).isZero();
        assertThat(pageDto.isHasNext()).isFalse();
        assertThat(pageDto.isHasPrevious()).isFalse();
    }
}