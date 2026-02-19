package ru.misis.gamification.dto.admin.request;

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
public class EventTypeCreateDto {

    /**
     * Код типа события
     */
    @NotBlank(message = "Код типа обязателен")
    String typeCode;

    /**
     * Название
     */
    @NotBlank(message = "Название обязательно")
    String displayName;

    /**
     * Количество баллов
     */
    @Min(value = 1, message = "Очки должны быть > 0")
    Integer points;

    /**
     * Максимальное число баллов в день
     */
    @Min(value = 0, message = "Лимит должен быть ≥ 0")
    Integer maxDailyPoints;
}
