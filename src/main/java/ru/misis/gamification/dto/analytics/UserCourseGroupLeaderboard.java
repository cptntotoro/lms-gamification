package ru.misis.gamification.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO с лидербордом по курсу для студента
 * <p>
 * Содержит топ-N участников + отдельную запись текущего пользователя (если зачислен).
 * </p>
 */
@Data
@Builder
@Schema(description = "Лидерборд по курсу для пользователя (топ + данные пользователя)")
public class UserCourseGroupLeaderboard {

    /**
     * Топ участников лидерборда (отсортированы по убыванию очков)
     */
    @Schema(description = "Топ участников лидерборда")
    private List<LeaderboardEntryDto> topEntries;

    /**
     * Запись текущего пользователя (null, если не зачислен на курс)
     */
    @Schema(description = "Запись пользователя в лидерборде (null, если не зачислен)")
    private LeaderboardEntryDto currentUserEntry;

    /**
     * Место пользователя в общем лидерборде курса (null, если не зачислен)
     */
    @Schema(description = "Место пользователя в общем лидерборде курса (null, если не зачислен)")
    private Long currentUserRank;

    /**
     * Очки пользователя по курсу (null, если не зачислен)
     */
    @Schema(description = "Очки пользователя по курсу (null, если не зачислен)")
    private Integer currentUserPoints;

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
     * Общее количество элементов в лидерборде (без учёта пользователя, если вне топа)
     */
    @Schema(description = "Общее количество элементов в лидерборде")
    private long totalElements;

    /**
     * Общее количество страниц
     */
    @Schema(description = "Общее количество страниц")
    private int totalPages;

    /**
     * Есть ли следующая страница
     */
    @Schema(description = "Есть ли следующая страница")
    private boolean hasNext;

    /**
     * Есть ли предыдущая страница
     */
    @Schema(description = "Есть ли предыдущая страница")
    private boolean hasPrevious;
}