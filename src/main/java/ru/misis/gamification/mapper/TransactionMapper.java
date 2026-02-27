package ru.misis.gamification.mapper;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import ru.misis.gamification.dto.admin.response.TransactionItemDto;
import ru.misis.gamification.dto.admin.response.TransactionPageDto;
import ru.misis.gamification.entity.Transaction;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер транзакций
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * Смаппить сущность транзакции в DTO транзакции для администратора
     *
     * @param transaction Транзакция
     * @return DTO транзакции для администратора
     */
    TransactionItemDto toDto(Transaction transaction);

    /**
     * Смаппить страницу транзакций в DTO страницы транзакций для администратора
     *
     * @param page Страница транзакций
     * @return DTO страницы транзакций для администратора
     */
    default TransactionPageDto toPageDto(Page<Transaction> page) {
        if (page == null) {
            return TransactionPageDto.builder()
                    .content(List.of())
                    .pageNumber(0)
                    .pageSize(0)
                    .totalElements(0L)
                    .totalPages(0)
                    .hasNext(false)
                    .hasPrevious(false)
                    .build();
        }

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
