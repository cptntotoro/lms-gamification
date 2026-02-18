package ru.misis.gamification.dto.admin.response;

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
public class UserAdminDto {

    /**
     * Идентификатор записи в таблице
     */
    private UUID uuid;

    /**
     * Идентификатор пользователя из LMS
     */
    private String userId;

    /**
     * Общее количество очков
     */
    private Integer totalPoints;

    /**
     * Уровень
     */
    private Integer level;

    /**
     * Дата создания записи
     */
    private LocalDateTime createdAt;

    /**
     * Дата обновления записи
     */
    private LocalDateTime updatedAt;
}