package ru.misis.gamification.dto.admin.response;

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
public class TransactionItemDto {

    /**
     * Идентификатор записи в таблице
     */
    private UUID uuid;

    /**
     * Идентификатор пользователя из LMS
     */
    private String userId;

    /**
     * Идентификатор события из LMS
     */
    private String eventId;

    /**
     * Количество начисленных очков
     */
    private Integer pointsEarned;

    /**
     * Описание события
     */
    private String description;

    /**
     * Дата создания записи
     */
    private LocalDateTime createdAt;
}