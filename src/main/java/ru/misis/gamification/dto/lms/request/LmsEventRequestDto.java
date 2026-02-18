package ru.misis.gamification.dto.lms.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO события из LMS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LmsEventRequestDto {

    /**
     * Идентификатор пользователя из LMS
     */
    @NotBlank(message = "userId не может быть пустым")
    private String userId;

    /**
     * Идентификатор события из LMS
     */
    @NotBlank(message = "userId не может быть пустым")
    private String eventId;

    /**
     * Количество начисленных очков
     */
    @Min(value = 1, message = "pointsEarned должен быть больше 0")
    private Integer pointsEarned;
}
