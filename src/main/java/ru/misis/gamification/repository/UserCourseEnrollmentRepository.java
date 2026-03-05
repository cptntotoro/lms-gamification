package ru.misis.gamification.repository;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.model.LeaderboardEntryView;

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
     * Получить страницу лидерборда студентов группы на курсе, отсортированную по убыванию очков
     * <p>
     * Если groupUuid = null — возвращает лидерборд по всему курсу (все группы/студенты без группы)
     * Если groupUuid указан — только студенты указанной группы
     * </p>
     *
     * @param courseUuid UUID курса
     * @param groupUuid  UUID группы
     * @param pageable   Параметры пагинации и сортировки
     * @return Страница {@link UserCourseEnrollment} с загруженными пользователями
     */
//    @Query("""
//                SELECT new ru.misis.gamification.dto.analytics.LeaderboardEntryDto(
//                    u.uuid,
//                    u.userId,
//                    uce.totalPointsInCourse,
//                    u.level,
//                    ROW_NUMBER() OVER (ORDER BY uce.totalPointsInCourse DESC)
//                )
//                FROM UserCourseEnrollment uce
//                JOIN uce.user u
//                WHERE (:courseUuid IS NOT NULL AND uce.course.uuid = :courseUuid)
//                  AND (:groupUuid IS NULL OR uce.group.uuid = :groupUuid)
//            """)
//    Page<LeaderboardEntryDto> findLeaderboardByCourseAndGroup(
//            @Param("courseUuid") @Nullable UUID courseUuid,
//            @Param("groupUuid") @Nullable UUID groupUuid,
//            Pageable pageable
//    );
    @Query("""
            SELECT new ru.misis.gamification.application.model.LeaderboardEntryView(
                u.uuid,
                u.userId,
                uce.totalPointsInCourse,
                u.level,
                ROW_NUMBER() OVER (ORDER BY uce.totalPointsInCourse DESC)
            )
            FROM UserCourseEnrollment uce
            JOIN uce.user u
            WHERE (:courseUuid IS NOT NULL AND uce.course.uuid = :courseUuid)
              AND (:groupUuid IS NULL OR uce.group.uuid = :groupUuid)
            """)
    Page<LeaderboardEntryView> findLeaderboardByCourseAndGroup(
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

    /**
     * Проверяет существование зачисления по внутренним UUID пользователя и курса.
     *
     * @param userUuid   UUID пользователя
     * @param courseUuid UUID курса
     * @return Да / Нет
     */
    boolean existsByUserUuidAndCourseUuid(@Nullable UUID userUuid, UUID courseUuid);

    /**
     * Получить общее количество очков, набранных пользователем на указанном курсе.
     * <p>
     * Если пользователь не зачислен на курс — возвращает {@link Optional#empty()}.
     * </p>
     *
     * @param userUuid   UUID пользователя
     * @param courseUuid UUID курса
     * @return Optional с количеством очков или пустой Optional
     */
    @Query("SELECT uce.totalPointsInCourse FROM UserCourseEnrollment uce " +
            "WHERE uce.user.uuid = :userUuid AND uce.course.uuid = :courseUuid")
    Optional<Integer> findTotalPointsInCourseByUserUuidAndCourseUuid(@Param("userUuid") UUID userUuid,
                                                                     @Param("courseUuid") UUID courseUuid);

    /**
     * Получить страницу лидерборда студентов на курсе
     * <p>
     * Ранг рассчитывается через {@code DENSE_RANK()}:
     * <ul>
     *     <li>студенты с одинаковыми очками получают одинаковый ранг</li>
     *     <li>следующий ранг не пропускается (dense rank)</li>
     * </ul>
     * <p>
     *
     * @param courseUuid UUID курса
     * @param groupUuid  UUID группы
     * @param userUuid   UUID пользователя
     * @return Ранг пользователя (1 = лидер) или {@code null}, если пользователь не найден/не зачислен
     */
    @Query(value = """
                SELECT rank
                    FROM (
                        SELECT
                            user_uuid,
                            DENSE_RANK() OVER (
                                ORDER BY total_points_in_course DESC
                            ) AS rank
                        FROM user_course_enrollments
                        WHERE course_uuid = :courseUuid
                          AND (:groupUuid IS NULL OR group_uuid = :groupUuid)
                    ) ranked
                    WHERE user_uuid = :userUuid
            """, nativeQuery = true)
    Long findRankByPointsInCourse(
            @Param("courseUuid") UUID courseUuid,
            @Param("groupUuid") UUID groupUuid,
            @Param("userUuid") UUID userUuid
    );
}