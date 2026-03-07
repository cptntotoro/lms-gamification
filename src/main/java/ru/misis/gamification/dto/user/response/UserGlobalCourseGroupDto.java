package ru.misis.gamification.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO пользователя с глобальными данными + опционально по курсу/группе
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO пользователя с глобальными данными + опционально по курсу/группе")
public class UserGlobalCourseGroupDto {

    /**
     * Идентификатор пользователя из LMS
     */
    @Schema(description = "Идентификатор пользователя из LMS", example = "stud-98765", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

    /**
     * Общее количество очков по всем курсам
     */
    @Schema(description = "Общее количество очков по всем курсам", example = "3420", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalPoints;

    /**
     * Глобальный уровень пользователя
     */
    @Schema(description = "Глобальный уровень пользователя", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer level;

    /**
     * Очки до следующего глобального уровня
     */
    @Schema(description = "Очки до следующего глобального уровня", example = "580", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pointsToNextLevel;

    /**
     * Процент прогресса до следующего глобального уровня (0–100)
     */
    @Schema(description = "Процент прогресса до следующего глобального уровня (0–100)", example = "74.1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double progressPercent;

    // Курсовые поля (присутствуют только если передан courseId)
    /**
     * Идентификатор курса из LMS
     */
    @Schema(description = "Идентификатор курса из LMS", example = "CS-101-2025")
    private String courseId;

    /**
     * Идентификатор группы из LMS
     */
    @Schema(description = "Идентификатор группы из LMS", example = "G-14")
    private String groupId;

    /**
     * Очки, заработанные именно в этом курсе
     */
    @Schema(description = "Очки, заработанные именно в этом курсе", example = "1450")
    private Integer pointsInCourse;

    /**
     * Место в лидерборде по всему курсу
     */
    @Schema(description = "Место в лидерборде по всему курсу", example = "18")
    private Long rankInCourse;

    /**
     * Место в лидерборде внутри группы (если groupId передан)
     */
    @Schema(description = "Место в лидерборде внутри группы (если groupId передан)", example = "4")
    private Long rankInGroup;
}