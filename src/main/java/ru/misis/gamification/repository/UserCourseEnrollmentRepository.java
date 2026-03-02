package ru.misis.gamification.repository;

import org.jspecify.annotations.Nullable;
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

    boolean existsByUserUuidAndCourseUuid(@Nullable UUID currentUserUuid, UUID courseUuid);

    /**
     * Возвращает общее количество очков, набранных пользователем на указанном курсе.
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

    @Query(value = """
                SELECT COUNT(*) + 1 
                FROM user_course_enrollments uce
                WHERE uce.course_uuid = :courseUuid
                  AND (:groupUuid IS NULL OR uce.group_uuid = :groupUuid)
                  AND uce.total_points_in_course > (
                      SELECT uce2.total_points_in_course 
                      FROM user_course_enrollments uce2 
                      WHERE uce2.user_uuid = :userUuid 
                        AND uce2.course_uuid = :courseUuid
                  )
            """, nativeQuery = true)
    Long findRankByPointsInCourse(
            @Param("courseUuid") UUID courseUuid,
            @Param("groupUuid") UUID groupUuid,
            @Param("userUuid") UUID userUuid
    );

    /**
     * Количество пользователей с очками > заданного (для расчёта ранга)
     *
     * @param courseUuid
     * @param totalPointsInCourse
     * @return
     */
    long countByCourseUuidAndTotalPointsInCourseGreaterThan(UUID courseUuid, Integer totalPointsInCourse);
}