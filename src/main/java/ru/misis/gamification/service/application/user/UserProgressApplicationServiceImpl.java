package ru.misis.gamification.service.application.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserService userSimpleService;

    /**
     * Сервис расчета уровня пользователя на основе накопленных очков
     */
    private final LevelCalculatorService levelCalculator;

    @Override
    public UserProgressView getProgress(String userId) {
        User user = userSimpleService.getUserByExternalId(userId);
        ProgressMetrics metrics = calculateMetrics(user);

        return new UserProgressView(
                user.getUserId(),
                user.getTotalPoints(),
                user.getLevel(),
                metrics.pointsToNextLevel(),
                metrics.progressPercent()
        );
    }

    private ProgressMetrics calculateMetrics(User user) {
        int points = user.getTotalPoints() != null ? user.getTotalPoints() : 0;
        int level = user.getLevel() != null ? user.getLevel() : 1;
        long pointsToNext = levelCalculator.pointsToNextLevel(level);
        double percent = pointsToNext > 0 ? Math.min((double) points / pointsToNext * 100, 100) : 100.0;
        return new ProgressMetrics(pointsToNext, percent);
    }
}
