package ru.misis.gamification.mapper;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import ru.misis.gamification.dto.admin.TransactionPageDto;
import ru.misis.gamification.dto.admin.response.TransactionItemDto;
import ru.misis.gamification.model.admin.Transaction;

import java.util.stream.Collectors;

/**
 * Маппер транзакций
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * Смаппить сущность транзакции в DTO для ответа клиенту
     *
     * @param transaction сущность из базы данных
     * @return DTO для передачи в API
     */
    TransactionItemDto toDto(Transaction transaction);

    /**
     * Смаппить страницу транзакций в страничный DTO-ответ
     *
     * @param page страница из Spring Data
     * @return DTO с содержимым страницы и метаинформацией
     */
    default TransactionPageDto toPageDto(Page<Transaction> page) {
        return TransactionPageDto.builder()
                .content(page.getContent().stream()
                        .map(this::toDto)
                        .collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
