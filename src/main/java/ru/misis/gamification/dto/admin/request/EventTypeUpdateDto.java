package ru.misis.gamification.dto.admin.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class EventTypeUpdateDto {

    /**
     * Название
     */
    @NotBlank
    String displayName;

    /**
     * Количество баллов
     */
    @Min(1)
    Integer points;

    /**
     * Максимальное число баллов в день
     */
    Integer maxDailyPoints;

    /**
     * Флаг активности
     */
    Boolean active;
}
