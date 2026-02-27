package ru.misis.gamification.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Элемент лидерборда группы по курсу
 */
@Data
@Builder
@Schema(description = "Элемент лидерборда группы по курсу")
public class LeaderboardEntryDto {

    /**
     * Внутренний идентификатор пользователя
     */
    @Schema(description = "Внутренний UUID пользователя")
    private UUID userUuid;

    /**
     * Идентификатор пользователя из LMS
     */
    @Schema(description = "ID пользователя из LMS")
    private String userId;

    /**
     * Очки, заработанные на этом курсе
     */
    @Schema(description = "Очки, заработанные на этом курсе")
    private Integer pointsInCourse;

    /**
     * Глобальный уровень пользователя
     */
    @Schema(description = "Глобальный уровень пользователя")
    private Integer globalLevel;

    /**
     * Позиция в лидерборде группы (1 = лидер)
     */
    @Schema(description = "Позиция в лидерборде группы (1 = лидер)")
    private Integer rank;
}