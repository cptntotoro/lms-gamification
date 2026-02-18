package ru.misis.gamification.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO страницы транзакций для администратора
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPageDto {

    /**
     * Список DTO транзакций для администратора
     */
    private List<TransactionItemDto> content;

    /**
     * Номер страницы (0-based)
     */
    private int pageNumber;

    /**
     * Размер страницы
     */
    private int pageSize;

    /**
     * Всего элементов
     */
    private long totalElements;

    /**
     * Всего страниц
     */
    private int totalPages;

    /**
     * Флаг наличия следующей страницы
     */
    private boolean hasNext;

    /**
     * Флаг наличия предыдущей страницы
     */
    private boolean hasPrevious;
}