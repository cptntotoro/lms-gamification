package ru.misis.gamification.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cобытие из LMS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LmsEvent {

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
}
