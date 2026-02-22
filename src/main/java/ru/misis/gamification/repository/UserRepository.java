package ru.misis.gamification.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import ru.misis.gamification.model.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий пользователей
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Получить пользователя по идентификатору пользователя из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Пользователь
     */
    Optional<User> findByUserId(String userId);

    /**
     * Проверить существование пользователя по идентификатору пользователя из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Да / Нет
     */
    boolean existsByUserId(String userId);

    /**
     *
     *
     * @param userId
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findByUserIdForUpdate(String userId);
}
