package ru.misis.gamification.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.misis.gamification.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью {@link User}.
 * Предоставляет методы поиска и проверки существования пользователей.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Получить пользователя по идентификатору пользователя из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Optional с пользователем или пустой, если не найден
     */
    Optional<User> findByUserId(String userId);

    /**
     * Получить пользователя по идентификатору пользователя из LMS
     * Метод помечен пессимистической блокировкой на запись (FOR UPDATE),
     * что позволяет безопасно обновлять пользователя в рамках одной транзакции.
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Optional с пользователем или пустой, если не найден
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    Optional<User> findByUserIdWithLock(String userId);

    /**
     * Проверить существование пользователя по идентификатору пользователя из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Да / Нет
     */
    boolean existsByUserId(String userId);
}
