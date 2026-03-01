package ru.misis.gamification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.misis.gamification.dto.analytics.LeaderboardEntryDto;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий связей пользователь — курс
 */
@Repository
public interface UserCourseEnrollmentRepository extends JpaRepository<UserCourseEnrollment, UUID> {

    /**
     * Проверить наличие пользователя на курсе
     *
     * @param user   Пользователь
     * @param course Курс (дисциплина)
     * @return Да / Нет
     */
    boolean existsByUserAndCourse(User user, Course course);

    /**
     * Получить страницу лидерборда студентов группы на курсе, отсортированную
     * по убыванию очков на курсе
     * <p>
     * Если курс или группа не существуют — возвращается пустая страница.
     * </p>
     *
     * @param courseUuid UUID курса
     * @param groupUuid  UUID группы
     * @param pageable   Параметры пагинации и сортировки
     * @return Страница {@link UserCourseEnrollment} с загруженными пользователями
     */
    @Query("""
                SELECT new ru.misis.gamification.dto.analytics.LeaderboardEntryDto(
                    u.uuid,
                    u.userId,
                    uce.totalPointsInCourse,
                    u.level,
                    ROW_NUMBER() OVER (ORDER BY uce.totalPointsInCourse DESC)
                )
                FROM UserCourseEnrollment uce
                JOIN uce.user u
                WHERE uce.course.uuid = :courseUuid
                  AND uce.group.uuid = :groupUuid
            """)
    Page<LeaderboardEntryDto> findLeaderboardByCourseAndGroup(
            @Param("courseUuid") UUID courseUuid,
            @Param("groupUuid") UUID groupUuid,
            Pageable pageable
    );

    /**
     * Получить связь пользователя на курсе
     *
     * @param user   Пользователь
     * @param course Курс (дисциплина)
     * @return Optional с зачислением на курс (связь пользователь — курс) или пустой, если не найден
     */
    Optional<UserCourseEnrollment> findByUserAndCourse(User user, Course course);
}