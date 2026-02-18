package ru.misis.gamification.dto.lms.response;

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
public class LmsEventResponsetDto {

    /**
     * Идентификатор пользователя из LMS
     */
    private String userId;

    /**
     * Идентификатор события из LMS
     */
    private String eventId;

    /**
     * Статус события (success, duplicate, error)
     */
    private String status;

    /**
     * Сообщение (опционально)
     */
    private String message;

    /**
     * Количество начисленных очков
     */
    private Integer pointsEarned;

    /**
     * Сумма очков
     */
    private Integer totalPoints;

    /**
     * Новый уровень
     */
    private Integer newLevel;

    /**
     * Флаг повышения уровня
     */
    private Boolean levelUp;

    /**
     * Идентификатор транзакции
     */
    private UUID transactionId;

    /**
     * Дата и время транзакции
     */
    private LocalDateTime processedAt;

    public static LmsEventResponsetDto success(String userId, Integer pointsEarned,
                                               Integer totalPoints, String eventId,
                                               UUID transactionId) {
        return LmsEventResponsetDto.builder()
                .status("success")
                .userId(userId)
                .eventId(eventId)
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
