package ru.misis.gamification.dto.lms.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.misis.gamification.model.admin.EventType;

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
     * Тип события {@link EventType#getTypeCode()}
     */
    @NotBlank(message = "eventType обязателен")
    private String eventType;
}
