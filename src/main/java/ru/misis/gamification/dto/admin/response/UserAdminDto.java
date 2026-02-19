package ru.misis.gamification.dto.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO пользователя для администратора
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Полная информация о пользователе для админ-панели")
public class UserAdminDto {

    /**
     * Идентификатор записи в таблице
     */
    @Schema(description = "Внутренний UUID записи", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID uuid;

    /**
     * Идентификатор пользователя из LMS
     */
    @Schema(description = "Идентификатор пользователя из LMS", example = "user-12345")
    private String userId;

    /**
     * Общее количество очков
     */
    @Schema(description = "Общее количество очков", example = "1250")
    private Integer totalPoints;

    /**
     * Уровень
     */
    @Schema(description = "Текущий уровень пользователя", example = "7")
    private Integer level;

    /**
     * Дата создания записи
     */
    @Schema(description = "Дата создания записи", example = "2026-01-15T10:00:00")
    private LocalDateTime createdAt;

    /**
     * Дата обновления записи
     */
    @Schema(description = "Дата последнего обновления", example = "2026-02-19T14:30:00", nullable = true)
    private LocalDateTime updatedAt;
}