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
}
