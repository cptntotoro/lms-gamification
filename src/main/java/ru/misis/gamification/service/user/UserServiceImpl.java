package ru.misis.gamification.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

        return userRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .userId(userId)
                            .totalPoints(0)
                            .level(1)
                            .build();

                    User saved = userRepository.save(newUser);
                    log.info("Создан новый пользователь: userId={}, id={}",
                            userId, saved.getUuid());
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

    /**
     * Проверить существование пользователя
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Да / Нет
     */
    private boolean isExists(String userId) {
        return userRepository.existsByUserId(userId);
    }
}
