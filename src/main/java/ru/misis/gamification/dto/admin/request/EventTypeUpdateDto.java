package ru.misis.gamification.dto.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO обновления типа события для LMS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на обновление типа события (частичное обновление)")
public class EventTypeUpdateDto {

    /**
     * Название
     */
    @NotBlank(message = "Название типа события обязательно")
    @Schema(description = "Отображаемое название типа события", example = "Квиз / Тест")
    private String displayName;

    /**
     * Количество баллов
     */
    @Min(value = 1, message = "Количество очков должно быть положительным")
    @Schema(description = "Количество очков за одно событие этого типа", example = "80")
    private Integer points;

    /**
     * Максимальное число баллов в день
     */
    @Min(value = 0, message = "Максимум в день должен быть ≥ 0")
    @Schema(description = "Максимальное количество очков в день по этому типу (null = без лимита)",
            example = "300", nullable = true)
    private Integer maxDailyPoints;

    /**
     * Флаг активности
     */
    @NotNull(message = "Флаг активности обязателен")
    @Schema(description = "Флаг активности типа события (true = активен, false = отключён)", example = "false")
    private Boolean active;
}
