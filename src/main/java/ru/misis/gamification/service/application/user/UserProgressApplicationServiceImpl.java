package ru.misis.gamification.service.application.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.model.ProgressMetrics;
import ru.misis.gamification.model.UserProgressView;
import ru.misis.gamification.service.simple.progress.LevelCalculatorService;
import ru.misis.gamification.service.simple.user.UserService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Validated
public class UserProgressApplicationServiceImpl implements UserProgressApplicationService {

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    /**
     * Сервис расчета уровня пользователя на основе накопленных очков
     */
    private final LevelCalculatorService levelCalculator;

    @Value("${gamification.progress.default-points:0}")
    private int defaultPoints;

    @Value("${gamification.progress.default-level:1}")
    private int defaultLevel;

    @Value("${gamification.progress.max-progress:100.0}")
    private double maxProgress;

    @Override
    public UserProgressView getProgress(String userId) {
        User user = userService.getUserByExternalId(userId);
        ProgressMetrics metrics = calculateMetrics(user);

        int safePoints = user.getTotalPoints() != null ? user.getTotalPoints() : defaultPoints;
        int safeLevel = user.getLevel() != null ? user.getLevel() : defaultLevel;

        return new UserProgressView(
                user.getUserId(),
                safePoints,
                safeLevel,
                metrics.pointsToNextLevel(),
                metrics.progressPercent()
        );
    }

    private ProgressMetrics calculateMetrics(User user) {
        int points = user.getTotalPoints() != null ? user.getTotalPoints() : defaultPoints;
        int level = user.getLevel() != null ? user.getLevel() : defaultLevel;

        long pointsToNext = levelCalculator.pointsToNextLevel(level);
        double percent = pointsToNext > 0
                ? Math.min((double) points / pointsToNext * 100, maxProgress)
                : maxProgress;

        return new ProgressMetrics(pointsToNext, percent);
    }
}
