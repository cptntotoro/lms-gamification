package ru.misis.gamification.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Элемент лидерборда группы по курсу
 */
@Data
@Builder
@Schema(description = "Элемент лидерборда группы по курсу")
@AllArgsConstructor
public class LeaderboardEntryDto {

    /**
     * Внутренний идентификатор пользователя
     */
    @Schema(description = "Внутренний UUID пользователя")
    private UUID userUuid;

    /**
     * Идентификатор пользователя из LMS
     */
    @Schema(description = "Идентификатор пользователя из LMS")
    private String userId;

//    private String displayName;      // полное имя или ник

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
    private Long rank;

    /**
     * Флаг текущего пользователя
     */
    @Schema(description = "Флаг текущего пользователя (true если запись принадлежит запрашивающему)")
    private Boolean isCurrentUser;

    public LeaderboardEntryDto(UUID userUuid, String userId, Integer pointsInCourse, Integer globalLevel, Long rank) {
        this.userUuid = userUuid;
        this.userId = userId;
        this.pointsInCourse = pointsInCourse;
        this.globalLevel = globalLevel;
        this.rank = rank;
    }
}