package ru.misis.gamification.dto.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Пагинированная страница транзакций для администратора")
public class TransactionPageDto {

    /**
     * Список DTO транзакций для администратора
     */
    @Schema(description = "Список элементов транзакций на текущей странице")
    private List<TransactionItemDto> content;

    /**
     * Номер страницы (0-based)
     */
    @Schema(description = "Номер текущей страницы (0-based)", example = "0")
    private int pageNumber;

    /**
     * Размер страницы
     */
    @Schema(description = "Размер страницы", example = "20")
    private int pageSize;

    /**
     * Всего элементов
     */
    @Schema(description = "Общее количество элементов", example = "150")
    private long totalElements;

    /**
     * Всего страниц
     */
    @Schema(description = "Общее количество страниц", example = "8")
    private int totalPages;

    /**
     * Флаг наличия следующей страницы
     */
    @Schema(description = "Есть ли следующая страница", example = "true")
    private boolean hasNext;

    /**
     * Флаг наличия предыдущей страницы
     */
    @Schema(description = "Есть ли предыдущая страница", example = "false")
    private boolean hasPrevious;
}