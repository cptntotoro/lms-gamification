package ru.misis.gamification.dto.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO типа события для администратора
 */
@Data
@Builder
@Schema(description = "Полная информация о типе события для администратора")
public class EventTypeDto {

    /**
     * Идентификатор записи в таблице
     */
    @Schema(description = "Внутренний UUID записи", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID uuid;

    /**
     * Код типа события
     */
    @Schema(description = "Уникальный код типа события", example = "quiz")
    private String typeCode;

    /**
     * Название
     */
    @Schema(description = "Отображаемое название", example = "Квиз / Тест")
    private String displayName;

    /**
     * Количество баллов
     */
    @Schema(description = "Количество очков за событие", example = "80")
    private Integer points;

    /**
     * Максимальное число баллов в день
     */
    @Schema(description = "Максимум очков в день (null = без ограничения)", example = "300", nullable = true)
    private Integer maxDailyPoints;

    /**
     * Флаг активности
     */
    @Schema(description = "Активен ли тип", example = "true")
    private boolean active;

    /**
     * Дата создания записи
     */
    @Schema(description = "Дата создания записи", example = "2026-02-19T14:30:00")
    private LocalDateTime createdAt;

    /**
     * Дата обновления записи
     */
    @Schema(description = "Дата последнего обновления", example = "2026-02-19T16:45:00", nullable = true)
    private LocalDateTime updatedAt;
}
