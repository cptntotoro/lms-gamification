package ru.misis.gamification.service.progress;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.dto.web.response.UserDto;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.service.progress.calculator.ProgressCalculator;
import ru.misis.gamification.service.progress.result.ProgressMetrics;
import ru.misis.gamification.service.user.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserProgressServiceImpl implements UserProgressService {

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    /**
     * Компонент расчёта метрик прогресса пользователя
     */
    private final ProgressCalculator progressCalculator;

    @Transactional(readOnly = true)
    @Override
    public UserDto getProgress(@NotBlank(message = "{user.id.required}") String userId) {
        log.debug("Получение прогресса для виджета: userId={}", userId);

        User user = userService.getUserByExternalId(userId);
        ProgressMetrics metrics = progressCalculator.calculate(user);

        return UserDto.builder()
                .userId(user.getUserId())
                .totalPoints(user.getTotalPoints())
                .level(user.getLevel())
                .pointsToNextLevel(metrics.getPointsToNextLevel())
                .progressPercent(metrics.getProgressPercent())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public UserAdminDto getAdminProgress(@NotBlank(message = "{user.id.required}") String userId) {
        log.debug("Получение прогресса для админ-профиля: userId={}", userId);

        User user = userService.getUserByExternalId(userId);
        ProgressMetrics metrics = progressCalculator.calculate(user);

        return UserAdminDto.builder()
                .uuid(user.getUuid())
                .userId(user.getUserId())
                .totalPoints(user.getTotalPoints())
                .level(user.getLevel())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .pointsToNextLevel(metrics.getPointsToNextLevel())
                .progressPercent(metrics.getProgressPercent())
                .build();
    }
}
