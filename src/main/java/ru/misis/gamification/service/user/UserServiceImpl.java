package ru.misis.gamification.service.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.repository.UserRepository;
import ru.misis.gamification.service.course.UserCourseService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserServiceImpl implements UserService {

    /**
     * Репозиторий пользователей
     */
    private final UserRepository userRepository;

    /**
     * Сервис управления курсами пользователей
     */
    private final UserCourseService userCourseService;

    @Override
    public User createIfNotExists(String userId) {
        return createIfNotExists(userId, null, null);
    }

    @Override
    public User createIfNotExists(@NotBlank(message = "{user.id.required}") String userId,
                                  String courseId,
                                  String groupId) {
        log.debug("Проверка/создание пользователя: userId={}, course={}, group={}",
                userId, courseId, groupId);

        return userRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> createNewUser(userId, courseId, groupId));
    }

    @Override
    public User get(@NotBlank(message = "{user.id.required}") String userId) throws UserNotFoundException {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: userId={}", userId);
                    return new UserNotFoundException(userId);
                });
    }

    @Override
    public User update(@NotNull(message = "{user.required}") User user) {
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
    public Page<User> findAll(@NotNull(message = "{pageable.required}") Pageable pageable) {
        log.debug("Запрос списка всех пользователей: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        return userRepository.findAll(pageable);
    }

    @Override
    public UUID getUserUuidByExternalId(@NotBlank(message = "{user.id.required}") String userId) {
        return userRepository.findUuidByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public User getUserByExternalId(@NotBlank(message = "{user.id.required}") String userId) throws UserNotFoundException {
        UUID userUuid = getUserUuidByExternalId(userId);
        User user = getByUuid(userUuid);

        log.debug("Пользователь найден по внешнему ID: userId={}, uuid={}", userId, userUuid);
        return user;
    }

    @Override
    public User getByUuid(@NotNull(message = "{user.uuid.required}") UUID uuid) throws UserNotFoundException {
        return userRepository.findById(uuid)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: uuid={}", uuid);
                    return new UserNotFoundException("Пользователь с UUID " + uuid + " не найден");
                });
    }

    private User createNewUser(String userId, String courseId, String groupId) {
        User newUser = User.builder()
                .userId(userId)
                .totalPoints(0)
                .level(1)
                .build();

        User savedUser = userRepository.save(newUser);
        userCourseService.enrollIfNeeded(savedUser, courseId, groupId);

        log.info("Создан новый пользователь: userId={}, uuid={}", userId, savedUser.getUuid());
        return savedUser;
    }
}
