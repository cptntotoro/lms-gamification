package ru.misis.gamification.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Страница лидерборда группы по курсу
 */
@Data
@Builder
@Schema(description = "Страница лидерборда группы по курсу")
public class GroupLeaderboardPageDto {

    /**
     * Список участников лидерборда
     */
    @Schema(description = "Список участников лидерборда")
    private List<LeaderboardEntryDto> content;

    /**
     * Номер текущей страницы (0-based)
     */
    @Schema(description = "Номер текущей страницы (0-based)")
    private int pageNumber;

    /**
     * Размер страницы
     */
    @Schema(description = "Размер страницы")
    private int pageSize;

    /**
     * Общее количество элементов
     */
    @Schema(description = "Общее количество элементов")
    private long totalElements;

    /**
     * Общее количество страниц
     */
    @Schema(description = "Общее количество страниц")
    private int totalPages;

    /**
     * Флаг следующей страницы
     */
    @Schema(description = "Есть ли следующая страница")
    private boolean hasNext;

    /**
     * Флаг предыдущей страницы
     */
    @Schema(description = "Есть ли предыдущая страница")
    private boolean hasPrevious;
}