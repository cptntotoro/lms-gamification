package ru.misis.gamification.dto.web.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO пользователя для виджета
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /**
     * Идентификатор пользователя из LMS
     */
    String userId;

    /**
     * Сумма очков
     */
    Integer totalPoints;

    /**
     * Текущий уровень
     */
    private Integer level;
}
