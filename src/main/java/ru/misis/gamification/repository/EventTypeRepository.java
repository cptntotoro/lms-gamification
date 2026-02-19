package ru.misis.gamification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.misis.gamification.model.admin.EventType;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий типов событий
 */
@Repository
public interface EventTypeRepository extends JpaRepository<EventType, UUID> {

    /**
     * Получить тип события из LMS по его коду
     *
     * @param typeCode Уникальный код типа события
     * @return Тип события из LMS
     */
    Optional<EventType> findByTypeCodeAndActiveTrue(String typeCode);

    /**
     * Проверить существование типа события из LMS по его коду
     *
     * @param typeCode Уникальный код типа события
     * @return Да / Нет
     */
    boolean existsByTypeCode(String typeCode);

    /**
     * Сумма очков, начисленных пользователю по конкретному типу события за указанный день
     *
     * @param userId     Идентификатор пользователя из LMS
     * @param typeCode   Уникальный код типа события
     * @param date       Дата (день), за который считаем
     * @return Сумма очков или 0, если записей нет
     */
    @Query("""
        SELECT COALESCE(SUM(t.pointsEarned), 0)
        FROM Transaction t
        WHERE t.userId = :userId
          AND t.eventTypeCode = :typeCode
          AND DATE(t.createdAt) = :date
    """)
    long sumPointsByUserIdAndEventTypeAndDate(
            String userId,
            String typeCode,
            LocalDate date
    );
}