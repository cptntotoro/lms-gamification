package ru.misis.gamification.service.point.result;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Внутренний результат операции начисления очков
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
    private final Integer newLevel;

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
     * Процент прогресса уровня
     */
    private final Double progressPercent;

    /**
     * Создаёт успешный результат начисления очков
     *
     * @param pointsEarned    Начисленные очки
     * @param totalAfter      Итоговое количество очков
     * @param levelAfter      Уровень после начисления
     * @param levelUp         Был ли level up
     * @param txId            Идентификатор транзакции
     * @param pointsToNext    Очки до следующего уровня
     * @param progressPercent Процент прогресса уровня
     * @return Успешный AwardResult
     */
    public static AwardResult success(
            int pointsEarned, int totalAfter, int levelAfter,
            boolean levelUp, UUID txId, long pointsToNext, double progressPercent) {
        return AwardResult.builder()
                .status(AwardStatus.SUCCESS)
                .pointsEarned(pointsEarned)
                .totalPointsAfter(totalAfter)
                .newLevel(levelAfter)
                .levelUp(levelUp)
                .transactionId(txId)
                .pointsToNextLevel(pointsToNext)
                .progressPercent(progressPercent)
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

