package ru.misis.gamification.service.application.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.model.UserSummary;
import ru.misis.gamification.service.simple.user.UserService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Validated
public class UserApplicationServiceImpl implements UserApplicationService {

    private final UserService userService;

    @Override
    public UserSummary createIfNotExists(String userId, String courseId, String groupId) {
        User user = userService.createIfNotExists(userId, courseId, groupId);
        return new UserSummary(user.getUuid(), user.getUserId(), user.getTotalPoints(), user.getLevel());
    }

    @Override
    public UserSummary getUserSummary(String userId) {
        User user = userService.getUserByExternalId(userId);
        return new UserSummary(user.getUuid(), user.getUserId(), user.getTotalPoints(), user.getLevel());
    }
}
