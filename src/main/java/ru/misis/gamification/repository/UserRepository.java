package ru.misis.gamification.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.misis.gamification.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий пользователей {@link User}
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

    /**
     * Получить UUID пользователя по идентификатору пользователя из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Optional с UUID пользователя или пустой, если не найден
     */
    @Query("SELECT u.uuid FROM User u WHERE u.userId = :userId")
    Optional<UUID> findUuidByUserId(@Param("userId") String userId);

    /**
     * Получить список пользователей с фильтрацией по курсу и группе
     * <p>
     * Если courseId = null — возвращаются все пользователи (без привязки к курсам).
     * Если courseId указан, но groupId = null — все пользователи курса (все группы).
     * Если указаны оба — пользователи конкретной группы в курсе.
     *
     * @param courseId Идентификатор курса из LMS
     * @param groupId  Идентификатор группы из LMS
     * @param pageable Параметры пагинации и сортировки
     * @return страница пользователей
     */
    @Query("""
            SELECT u
            FROM User u
            LEFT JOIN UserCourseEnrollment e ON e.user = u
            LEFT JOIN e.course c
            LEFT JOIN e.group g
            WHERE (:courseId IS NULL OR c.courseId = :courseId)
              AND (:groupId IS NULL OR g.groupId = :groupId)
            """)
    Page<User> findAll(@Param("courseId") String courseId,
                       @Param("groupId") String groupId,
                       Pageable pageable);
}
