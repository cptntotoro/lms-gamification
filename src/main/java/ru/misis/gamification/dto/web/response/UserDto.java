package ru.misis.gamification.dto.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO пользователя для виджета
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Прогресс пользователя для отображения в виджете")
public class UserDto {

    /**
     * Идентификатор пользователя из LMS
     */
    @Schema(description = "Идентификатор пользователя из LMS", example = "user-12345")
    String userId;

    /**
     * Сумма очков
     */
    @Schema(description = "Общее количество очков", example = "1250")
    Integer totalPoints;

    /**
     * Текущий уровень
     */
    @Schema(description = "Текущий уровень пользователя", example = "7")
    private Integer level;

    /**
     * Количество очков до следующего уровня
     */
    @Schema(description = "Количество очков до следующего уровня", example = "750")
    private Long pointsToNextLevel;

    /**
     * Процент прогресса до следующего уровня (0-100)
     */
    @Schema(description = "Процент прогресса до следующего уровня (0-100)", example = "62.5")
    private Double progressPercent;
}
