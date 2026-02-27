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
     * Получить связь пользователя на курсе
     *
     * @param user     Пользователь
     * @param courseId Идентификатор курса из LMS
     * @return Optional со связью пользователь — курс или пустой, если не найден
     */
    Optional<UserCourseEnrollment> findByUserAndCourseCourseId(User user, String courseId);

    /**
     * Получить страницу лидерборда студентов группы на курсе, отсортированную
     * по убыванию очков на курсе
     * <p>
     * Использует оконную функцию {@code ROW_NUMBER()} для расчёта глобальной позиции
     * (rank) по убыванию очков на курсе. Ранг считается по всей выборке, а не только
     * по текущей странице.
     * </p>
     * <p>
     * Выполняет жадную загрузку (JOIN FETCH) сущности {@code User}, чтобы избежать
     * N+1 запросов при работе с данными студента.
     * </p>
     * <p>
     * Если курс или группа не существуют — возвращается пустая страница.
     * </p>
     *
     * @param courseId Идентификатор курса из LMS
     * @param groupId  Идентификатор группы из LMS
     * @param pageable Параметры пагинации и сортировки
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
                WHERE uce.course.uuid = :courseId
                  AND uce.group.uuid = :groupId
            """)
    Page<LeaderboardEntryDto> findLeaderboardByCourseAndGroup(@Param("courseId") String courseId, @Param("groupId") String groupId, Pageable pageable);
}