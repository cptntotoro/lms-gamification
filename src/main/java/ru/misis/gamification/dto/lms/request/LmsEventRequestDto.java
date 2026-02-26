package ru.misis.gamification.dto.lms.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Запрос от LMS на обработку события (начисление очков)")
public class LmsEventRequestDto {

    /**
     * Идентификатор пользователя из LMS
     */
    @NotBlank(message = "userId не может быть пустым")
    @Schema(description = "Идентификатор пользователя из LMS", example = "user-12345")
    private String userId;

    /**
     * Идентификатор события из LMS
     */
    @NotBlank(message = "userId не может быть пустым")
    @Schema(description = "Уникальный идентификатор события из LMS (защита от дублей)",
            example = "event-uuid-001")
    private String eventId;

    /**
     * Тип события {@link EventType#getTypeCode()}
     */
    @NotBlank(message = "eventType обязателен")
    @Schema(description = "Код типа события (должен существовать в системе)", example = "quiz")
    private String eventType;

    /**
     * Уникальный идентификатор курса из LMS
     */
    @Schema(description = "Внешний идентификатор курса из LMS", example = "MATH101")
    private String courseId;

    /**
     * Уникальный идентификатор группы/потока из LMS
     */
    @Schema(description = "Внешний идентификатор группы/потока из LMS", example = "1-A")
    private String groupId;
}
