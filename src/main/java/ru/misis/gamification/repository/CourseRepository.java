package ru.misis.gamification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.misis.gamification.entity.Course;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий курсов
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    /**
     * Получить курс по идентификатору курса из LMS
     *
     * @param courseId Идентификатор курса из LMS
     * @return Optional с курсом или пустой, если не найден
     */
    Optional<Course> findByCourseId(String courseId);

    /**
     * Проверить существование курса по идентификатору курса из LMS
     *
     * @param courseId Идентификатор курса из LMS
     * @return Да / Нет
     */
    boolean existsByCourseId(String courseId);
}