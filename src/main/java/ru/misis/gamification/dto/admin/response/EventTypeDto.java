package ru.misis.gamification.dto.admin.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO типа события для администратора
 */
@Data
@Builder
public class EventTypeDto {

    /**
     * Идентификатор записи в таблице
     */
    private UUID uuid;

    /**
     * Код типа события
     */
    private String typeCode;

    /**
     * Название
     */
    private String displayName;

    /**
     * Количество баллов
     */
    private Integer points;

    /**
     * Максимальное число баллов в день
     */
    private Integer maxDailyPoints;

    /**
     * Флаг активности
     */
    private boolean active;

    /**
     * Дата создания записи
     */
    private LocalDateTime createdAt;

    /**
     * Дата обновления записи
     */
    private LocalDateTime updatedAt;
}
