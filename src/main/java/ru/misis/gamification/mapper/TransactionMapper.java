package ru.misis.gamification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import ru.misis.gamification.dto.admin.response.TransactionItemDto;
import ru.misis.gamification.dto.admin.response.TransactionPageDto;
import ru.misis.gamification.entity.Transaction;
import ru.misis.gamification.model.TransactionSummary;

/**
 * Маппер транзакций
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {
    /**
     * Смаппить модель транзакции в DTO транзакции для администратора
     *
     * @param summary Модель транзакции
     * @return DTO транзакции для администратора
     */
    TransactionItemDto toTransactionItemDto(TransactionSummary summary);

    /**
     * Смаппить страницу модели транзакции в DTO страницы транзакций для администратора
     *
     * @param page Страница модели транзакции
     * @return DTO страницы транзакций для администратора
     */
    default TransactionPageDto toTransactionPageDto(Page<TransactionSummary> page) {
        if (page == null) {
            return null;
        }
        return TransactionPageDto.builder()
                .content(page.getContent().stream().map(this::toTransactionItemDto).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * Смаппить транзакцию в модель транзакции
     *
     * @param transaction Транзакция
     * @return Модель транзакции
     */
    @Mapping(target = "userId", source = "user.userId")
    TransactionSummary toTransactionSummary(Transaction transaction);
}
