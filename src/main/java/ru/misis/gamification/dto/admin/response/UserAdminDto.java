package ru.misis.gamification.dto.admin.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
     * Количество очков до следующего уровня
     */
    @Schema(description = "Количество очков до следующего уровня", example = "750")
    private Long pointsToNextLevel;

    /**
     * Процент прогресса до следующего уровня (0-100)
     */
    @Schema(description = "Процент прогресса до следующего уровня (0-100)", example = "62.5")
    private Double progressPercent;

    /**
     * Дата создания записи
     */
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    @Schema(description = "Дата создания записи", example = "19.02.2026 14:30")
    private LocalDateTime createdAt;

    /**
     * Дата обновления записи
     */
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    @Schema(description = "Дата последнего обновления", example = "19.02.2026 14:30", nullable = true)
    private LocalDateTime updatedAt;
}