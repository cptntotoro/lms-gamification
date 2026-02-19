package ru.misis.gamification.dto.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO транзакции для администратора
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Элемент транзакции для отображения в админ-панели")
public class TransactionItemDto {

    /**
     * Идентификатор записи в таблице
     */
    @Schema(description = "Внутренний UUID транзакции", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private UUID uuid;

    /**
     * Идентификатор пользователя из LMS
     */
    @Schema(description = "Идентификатор пользователя из LMS", example = "user-12345")
    private String userId;

    /**
     * Идентификатор события из LMS
     */
    @Schema(description = "Уникальный идентификатор события из LMS", example = "event-uuid-001")
    private String eventId;

    /**
     * Количество начисленных очков
     */
    @Schema(description = "Количество начисленных очков", example = "80")
    private Integer pointsEarned;

    /**
     * Описание события
     */
    @Schema(description = "Описание события", example = "Тип события: Квиз / Тест", nullable = true)
    private String description;

    /**
     * Дата создания записи
     */
    @Schema(description = "Дата и время начисления", example = "2026-02-19T15:30:00")
    private LocalDateTime createdAt;
}