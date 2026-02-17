package ru.misis.gamification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.misis.gamification.model.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий пользователей
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Получить пользователя по userId из LMS
     *
     * @param userId userId из LMS
     * @return Пользователь
     */
    Optional<User> findByUserId(String userId);

    /**
     * Проверить существование пользователя по userId из LMS
     *
     * @param userId userId из LMS
     * @return Да / Нет
     */
    boolean existsByUserId(String userId);

    /**
     * Получить пользователей по убыванию количества баллов
     *
     * @return Список пользователей
     */
    @Query("SELECT u FROM User u ORDER BY u.totalPoints DESC")
    List<User> findAllOrderByPointsDesc();

    /**
     * Получить 10 лучших пользователей по убыванию количества баллов
     *
     * @return Список пользователей
     */
    List<User> findTop10ByOrderByTotalPointsDesc();
}
