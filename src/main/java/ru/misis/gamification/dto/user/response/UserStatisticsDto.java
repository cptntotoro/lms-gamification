package ru.misis.gamification.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO пользователя в контексте курса и (опционально) группы
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные пользователя в контексте курса и (опционально) группы")
public class UserStatisticsDto {

    /**
     * Идентификатор пользователя из LMS
     */
    @Schema(description = "Идентификатор пользователя из LMS", example = "stud-98765")
    private String userId;

    /**
     * Общий уровень пользователя
     */
    @Schema(description = "Глобальный уровень пользователя", example = "12")
    private Integer globalLevel;

    /**
     * Общее количество очков пользователя
     */
    @Schema(description = "Общее количество очков по всем курсам", example = "3420")
    private Integer totalPoints;

    // Контекстные (по курсу / группе)
    /**
     * Идентификатор курса из LMS
     */
    @Schema(description = "Идентификатор курса из LMS", example = "CS-101-2025", requiredMode = Schema.RequiredMode.REQUIRED)
    private String courseId;

    /**
     * Идентификатор группы из LMS
     */
    @Schema(description = "Идентификатор группы из LMS", example = "G-14")
    private String groupId;

    /**
     * Очки пользователем именно в этом курсе
     */
    @Schema(description = "Очки пользователем именно в этом курсе", example = "1450")
    private Integer pointsInCourse;

    /**
     * Место пользователя на всем курсе среди всех групп
     */
    @Schema(description = "Место пользователя на всем курсе среди всех групп", example = "18")
    private Long rankInCourse;

    /**
     * Место пользователя в группе
     */
    @Schema(description = "Место пользователя в группе (если groupId указан)", example = "4")
    private Long rankInGroup;

    /**
     * Очки для следующего глобального уровня
     */
    @Schema(description = "Очки, необходимые для следующего глобального уровня", example = "580")
    private Long pointsToNextGlobalLevel;

    /**
     * Процент прогресса до следующего глобального уровня
     */
    @Schema(description = "Процент прогресса до следующего глобального уровня (0–100)", example = "74.1")
    private Double progressPercent;
}
