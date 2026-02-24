package ru.misis.gamification.dto.result;

import lombok.Builder;
import lombok.Getter;
import ru.misis.gamification.service.point.PointsService;

import java.util.UUID;

/**
 * Результат операции начисления очков за событие.
 * <p>
 * Класс является иммутабельным (все поля final) и используется для возврата результата из метода
 * {@link PointsService#awardPoints(LmsEventRequestDto)}.
 * </p>
 * <p>
 * Возможные статусы:
 * <ul>
 *     <li>{@link AwardStatus#SUCCESS} — очки успешно начислены</li>
 *     <li>{@link AwardStatus#DUPLICATE} — событие уже было обработано ранее (дубликат)</li>
 *     <li>{@link AwardStatus#REJECTED} — начисление отклонено (например, превышен лимит)</li>
 * </ul>
 * </p>
 */
@Getter
@Builder
public class AwardResult {

    /**
     * Статус операции начисления очков
     */
    private final AwardStatus status;

    /**
     * Количество начисленных очков (при успехе)
     */
    private final Integer pointsEarned;

    /**
     * Общее количество очков после начисления (при успехе)
     */
    private final Integer totalPointsAfter;

    /**
     * Уровень пользователя после начисления (при успехе)
     */
    private final Integer levelAfter;

    /**
     * Флаг переходd на новый уровень
     */
    private final boolean levelUp;

    /**
     * Причина отклонения начисления (при статусе REJECTED)
     */
    private final String rejectionReason;

    /**
     * Идентификатор созданной транзакции (при успехе)
     */
    private final UUID transactionId;

    /**
     * Количество очков, необходимых для достижения следующего уровня (при успехе)
     */
    private final Long pointsToNextLevel;

    /**
     * Создаёт успешный результат начисления очков.
     *
     * @param pointsEarned Начисленные очки
     * @param totalAfter   Итоговое количество очков
     * @param levelAfter   Уровень после начисления
     * @param levelUp      Был ли level up
     * @param txId         Идентификатор транзакции
     * @param pointsToNext Очки до следующего уровня
     * @return Успешный AwardResult
     */
    public static AwardResult success(int pointsEarned, int totalAfter, int levelAfter, boolean levelUp, UUID txId, long pointsToNext) {
        return AwardResult.builder()
                .status(AwardStatus.SUCCESS)
                .pointsEarned(pointsEarned)
                .totalPointsAfter(totalAfter)
                .levelAfter(levelAfter)
                .levelUp(levelUp)
                .transactionId(txId)
                .pointsToNextLevel(pointsToNext)
                .build();
    }

    /**
     * Создаёт результат, указывающий на дубликат события
     *
     * @return AwardResult со статусом DUPLICATE
     */
    public static AwardResult duplicate() {
        return AwardResult.builder().status(AwardStatus.DUPLICATE).build();
    }


    /**
     * Создаёт результат отклонения начисления с указанной причиной
     *
     * @param reason Текстовое объяснение причины отклонения
     * @return AwardResult со статусом REJECTED
     */
    public static AwardResult rejected(String reason) {
        return AwardResult.builder()
                .status(AwardStatus.REJECTED)
                .rejectionReason(reason)
                .build();
    }

    /**
     * Проверяет, было ли начисление успешно выполнено
     *
     * @return true если статус = SUCCESS, иначе false
     */
    public boolean isSuccess() {
        return status == AwardStatus.SUCCESS;
    }
}

