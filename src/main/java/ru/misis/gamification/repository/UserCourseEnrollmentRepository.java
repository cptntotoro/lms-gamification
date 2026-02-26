package ru.misis.gamification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.misis.gamification.model.entity.Course;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.model.entity.UserCourseEnrollment;

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
}