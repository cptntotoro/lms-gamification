package ru.misis.gamification.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AwardResult {
    private final AwardStatus status;
    private final Integer pointsEarned;
    private final Integer totalPointsAfter;
    private final Integer levelAfter;
    private final boolean levelUp;
    private final String rejectionReason;
    private final UUID transactionId;
    private final Long pointsToNextLevel;

    public static AwardResult success(int pointsEarned, int totalAfter, int levelAfter, boolean levelUp, UUID txId) {
        return AwardResult.builder()
                .status(AwardStatus.SUCCESS)
                .pointsEarned(pointsEarned)
                .totalPointsAfter(totalAfter)
                .levelAfter(levelAfter)
                .levelUp(levelUp)
                .transactionId(txId)
                .build();
    }

    public static AwardResult duplicate() {
        return AwardResult.builder().status(AwardStatus.DUPLICATE).build();
    }

    public static AwardResult rejected(String reason) {
        return AwardResult.builder()
                .status(AwardStatus.REJECTED)
                .rejectionReason(reason)
                .build();
    }

    public boolean isSuccess() {
        return status == AwardStatus.SUCCESS;
    }
}

