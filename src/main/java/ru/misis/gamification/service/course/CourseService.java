package ru.misis.gamification.service.course;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.exception.CourseNotFoundException;

import java.util.UUID;

/**
 * Сервис управления курсами (дисциплинами) {@link Course}
 */
public interface CourseService {

    /**
     * Проверить существование курса по идентификатору из LMS
     *
     * @param courseId Идентификатор курса из LMS
     * @return Да / Нет
     * @throws ConstraintViolationException если courseId == null или пустая строка
     */
    boolean existsByCourseId(@NotBlank(message = "{course.id.required}") String courseId);

    /**
     * Получить UUID курса по идентификатору из LMS
     *
     * @param courseId Идентификатор курса из LMS
     * @return UUID курса
     * @throws CourseNotFoundException      если курс с указанным courseId не найден
     * @throws ConstraintViolationException если courseId == null или пустая строка
     */
    UUID getCourseUuidByExternalId(@NotBlank(message = "{course.id.required}") String courseId);

    /**
     * Получить курс по идентификатору из LMS
     *
     * @param courseId Идентификатор курса из LMS
     * @return Курс {@link Course}
     * @throws CourseNotFoundException      если курс с указанным courseId не найден
     * @throws ConstraintViolationException если courseId == null или пустая строка
     */
    Course findByCourseId(@NotBlank(message = "{course.id.required}") String courseId);

    /**
     * Получить курс по UUID
     *
     * @param courseUuid UUID курса
     * @return Курс {@link Course}
     * @throws CourseNotFoundException      если курс с указанным UUID не найден
     * @throws ConstraintViolationException если courseUuid == null
     */
    Course findById(@NotNull(message = "{course.uuid.required}") UUID courseUuid);
}
