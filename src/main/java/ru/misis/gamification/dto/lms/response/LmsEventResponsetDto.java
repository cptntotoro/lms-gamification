package ru.misis.gamification.dto.lms.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ответ LMS-системе на обработанное событие.
 * Содержит статус обработки и актуальные данные о прогрессе пользователя
 */
@Data
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
//                                               Integer newLevel,
//                                               Boolean levelUp,
                                               UUID transactionId) {
        LmsEventResponsetDto response = new LmsEventResponsetDto();
        response.setStatus("success");
        response.setUserId(userId);
        response.setPointsEarned(pointsEarned);
        response.setTotalPoints(totalPoints);
//        response.setNewLevel(newLevel);
//        response.setLevelUp(levelUp);
        response.setTransactionId(transactionId);
        response.setProcessedAt(LocalDateTime.now());
        return response;
    }

    public static LmsEventResponsetDto duplicate(String eventId) {
        LmsEventResponsetDto response = new LmsEventResponsetDto();
        response.setStatus("duplicate");
        response.setMessage("Событие с ID " + eventId + " уже обработано");
        response.setProcessedAt(LocalDateTime.now());
        return response;
    }

    public static LmsEventResponsetDto error(String message) {
        LmsEventResponsetDto dto = new LmsEventResponsetDto();
        dto.setStatus("error");
        dto.setMessage(message != null ? message : "Внутренняя ошибка обработки события");
        dto.setProcessedAt(LocalDateTime.now());
        return dto;
    }
}
