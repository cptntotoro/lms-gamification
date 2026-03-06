package ru.misis.gamification.service.application.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.model.UserAdminView;
import ru.misis.gamification.service.simple.user.UserService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Validated
public class UserAdminApplicationServiceImpl implements UserAdminApplicationService {

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    /**
     * Фасадный сервис управления прогрессом очков и уровня пользователей
     */
    private final UserProgressApplicationService progressApplicationService;

    @Override
    public UserAdminView findByUserId(String userId) {
        User user = userService.getUserByExternalId(userId);
        log.debug("Админ запросил пользователя: userId={}", userId);
        return toUserAdminView(user);
    }

    @Override
    public Page<UserAdminView> findAll(Pageable pageable) {
        log.debug("Админ запросил список пользователей: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return userService.findAll(pageable)
                .map(this::toUserAdminView);
    }

    private UserAdminView toUserAdminView(User user) {
        var progress = progressApplicationService.getProgress(user.getUserId());
        return new UserAdminView(
                user.getUuid(),
                user.getUserId(),
                user.getTotalPoints(),
                user.getLevel(),
                progress.pointsToNextLevel(),
                progress.progressPercent(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
