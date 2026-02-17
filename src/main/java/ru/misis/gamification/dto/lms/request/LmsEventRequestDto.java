package ru.misis.gamification.dto.lms.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на событие из LMS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LmsEventRequestDto {

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
