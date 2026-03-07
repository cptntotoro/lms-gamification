package ru.misis.gamification.service.simple.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.events.UserCreatedEvent;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    /**
     * Репозиторий пользователей
     */
    private final UserRepository userRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Value("${gamification.user.default.initial-points:0}")
    private int initialPoints;

    @Value("${gamification.user.default.initial-level:1}")
    private int initialLevel;

    @Override
    public User createIfNotExists(String userId) {
        return createIfNotExists(userId, null, null);
    }

    @Override
    public User createIfNotExists(String userId,
                                  String courseId,
                                  String groupId) {
        log.debug("Проверка/создание пользователя: userId={}, course={}, group={}",
                userId, courseId, groupId);

        return userRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> createNewUser(userId, courseId, groupId));
    }

    @Override
    public User get(String userId) throws UserNotFoundException {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: userId={}", userId);
                    return new UserNotFoundException(userId);
                });
    }

    @Override
    public User update(User user) {
        if (user.getUuid() == null) {
            throw new IllegalArgumentException("Пользователь должен иметь uuid для обновления");
        }

        User updated = userRepository.save(user);
        log.debug("Пользователь обновлён: userId={}, totalPoints={}, level={}",
                updated.getUserId(), updated.getTotalPoints(), updated.getLevel());
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        log.debug("Запрос списка всех пользователей: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        return userRepository.findAll(pageable);
    }

    @Override
    public UUID getUserUuidByExternalId(String userId) {
        return userRepository.findUuidByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public User getUserByExternalId(String userId) throws UserNotFoundException {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public User getByUuid(UUID uuid) throws UserNotFoundException {
        return userRepository.findById(uuid)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: uuid={}", uuid);
                    return new UserNotFoundException("Пользователь с UUID " + uuid + " не найден");
                });
    }

    private User createNewUser(String userId, String courseId, String groupId) {
        User newUser = User.builder()
                .userId(userId)
                .totalPoints(initialPoints)
                .level(initialLevel)
                .build();

        User savedUser = userRepository.save(newUser);
        eventPublisher.publishEvent(new UserCreatedEvent(userId, courseId, groupId));

        log.info("Создан новый пользователь: userId={}, uuid={}", userId, savedUser.getUuid());
        return savedUser;
    }
}
