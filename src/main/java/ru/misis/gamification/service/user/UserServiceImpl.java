package ru.misis.gamification.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    /**
     * Репозиторий пользователей
     */
    private final UserRepository userRepository;

    @Override
    public User createIfNotExists(String userId) {
        log.debug("Проверка/создание пользователя: userId={}", userId);

        return userRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .userId(userId)
                            .totalPoints(0)
                            .level(1)
                            .build();

                    User saved = userRepository.save(newUser);
                    log.info("Создан новый пользователь: userId={}, uuid={}", userId, saved.getUuid());
                    return saved;
                });
    }

    @Override
    public User get(String userId) throws UserNotFoundException {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: userId={}", userId);
                    return new UserNotFoundException(userId);
                });
    }

    @Transactional
    @Override
    public User getOrCreateLocked(String userId) {
        log.debug("Получение пользователя с блокировкой: userId={}", userId);

        return userRepository.findByUserIdWithLock(userId)
                .orElseGet(() -> {
                    log.info("Создаём нового пользователя: {}", userId);
                    User newUser = User.builder()
                            .userId(userId)
                            .totalPoints(0)
                            .level(1)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    @Override
    public User update(User user) {
        if (user.getUuid() == null) {
            log.error("Попытка обновить пользователя без ID: userId={}", user.getUserId());
            throw new IllegalArgumentException("Нельзя обновлять пользователя без внутреннего ID");
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
}
