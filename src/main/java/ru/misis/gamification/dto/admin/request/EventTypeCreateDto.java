package ru.misis.gamification.dto.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO создания типа события для LMS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание типа события")
public class EventTypeCreateDto {

    /**
     * Код типа события
     */
    @NotBlank(message = "Код типа обязателен")
    @Schema(description = "Уникальный код типа (например: quiz, lab)", example = "quiz")
    private String typeCode;

    /**
     * Название
     */
    @NotBlank(message = "Название обязательно")
    @Schema(description = "Отображаемое название", example = "Квиз / Тест")
    private String displayName;

    /**
     * Количество баллов
     */
    @Min(value = 1, message = "Очки должны быть > 0")
    @Schema(description = "Количество очков за событие", example = "80")
    private Integer points;

    /**
     * Максимальное число баллов в день
     */
    @Min(value = 0, message = "Лимит должен быть ≥ 0")
    @Schema(description = "Максимум очков в день (null = без лимита)", example = "300", nullable = true)
    private Integer maxDailyPoints;
}
