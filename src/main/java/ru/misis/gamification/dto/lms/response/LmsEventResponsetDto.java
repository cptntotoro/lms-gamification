package ru.misis.gamification.dto.lms.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO ответа LMS-системе на обработанное событие.
 * Содержит статус обработки и актуальные данные о прогрессе пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ LMS-системе после обработки события")
public class LmsEventResponsetDto {

    /**
     * Идентификатор пользователя из LMS
     */
    @Schema(description = "Идентификатор пользователя из LMS", example = "user-12345")
    private String userId;

    /**
     * Идентификатор события из LMS
     */
    @Schema(description = "Идентификатор события из LMS", example = "event-uuid-001")
    private String eventId;

    /**
     * Название для отображения
     */
    @Schema(description = "Отображаемое название типа события", example = "Квиз / Тест", nullable = true)
    private String displayName;

    /**
     * Статус события (success, duplicate, error)
     */
    @Schema(description = "Статус обработки",
            allowableValues = {"success", "duplicate", "error"},
            example = "success")
    private String status;

    /**
     * Сообщение (опционально)
     */
    @Schema(description = "Сообщение об ошибке или причине (при status = error/duplicate)",
            example = "Событие уже обработано ранее", nullable = true)
    private String message;

    /**
     * Количество начисленных очков
     */
    @Schema(description = "Количество начисленных очков за это событие", example = "80", nullable = true)
    private Integer pointsEarned;

    /**
     * Сумма очков
     */
    @Schema(description = "Общее количество очков пользователя после начисления", example = "1250", nullable = true)
    private Integer totalPoints;

    /**
     * Новый уровень
     */
    @Schema(description = "Новый уровень пользователя (если изменился)", example = "7", nullable = true)
    private Integer newLevel;

    /**
     * Флаг повышения уровня
     */
    @Schema(description = "Флаг повышения уровня в этом событии", example = "true", nullable = true)
    private Boolean levelUp;

    private Long pointsToNextLevel;

    /**
     * Идентификатор транзакции
     */
    @Schema(description = "UUID созданной транзакции", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", nullable = true)
    private UUID transactionId;

    /**
     * Дата и время транзакции
     */
    @Schema(description = "Дата и время обработки события", example = "2026-02-19T16:30:00")
    private LocalDateTime processedAt;

    public static LmsEventResponsetDto success(String userId, Integer pointsEarned,
                                               Integer totalPoints, String eventId,
                                               UUID transactionId, String displayName) {
        return LmsEventResponsetDto.builder()
                .status("success")
                .userId(userId)
                .eventId(eventId)
                .displayName(displayName)
                .pointsEarned(pointsEarned)
                .totalPoints(totalPoints)
                .transactionId(transactionId)
                .processedAt(LocalDateTime.now())
                .build();
    }

    public static LmsEventResponsetDto duplicate(String eventId) {
        return LmsEventResponsetDto.builder()
                .status("duplicate")
                .eventId(eventId)
                .message("Событие с ID " + eventId + " уже обработано ранее")
                .processedAt(LocalDateTime.now())
                .build();
    }

    public static LmsEventResponsetDto error(String message) {
        return LmsEventResponsetDto.builder()
                .status("error")
                .message(message != null ? message : "Внутренняя ошибка обработки события")
                .processedAt(LocalDateTime.now())
                .build();
    }

    public boolean isSuccess() {
        return "success".equals(status);
    }

    public boolean isDuplicate() {
        return "duplicate".equals(status);
    }

    public boolean isError() {
        return "error".equals(status);
    }
}
