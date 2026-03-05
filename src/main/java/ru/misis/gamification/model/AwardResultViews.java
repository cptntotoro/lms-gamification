package ru.misis.gamification.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Фабрика для создания экземпляров AwardResultView.
 * <p>
 * Используется вместо статических методов внутри record, чтобы сохранить
 * чистоту record (только данные + accessor'ы).
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AwardResultViews {

    public static AwardResultView success(
            int pointsEarned,
            int totalPointsAfter,
            boolean levelUp,
            int newLevel,
            long pointsToNextLevel,
            double progressPercent) {
        return new AwardResultView(
                true,
                pointsEarned,
                totalPointsAfter,
                levelUp,
                newLevel,
                pointsToNextLevel,
                progressPercent,
                null,
                false
        );
    }

    public static AwardResultView duplicate() {
        return new AwardResultView(
                false, 0, 0, false, 0, 0L, 0.0,
                null, true
        );
    }

    public static AwardResultView rejected(String reason) {
        return new AwardResultView(
                false, 0, 0, false, 0, 0L, 0.0,
                reason, false
        );
    }
}