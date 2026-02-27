package ru.misis.gamification.service.course;

import ru.misis.gamification.exception.CourseNotFoundException;

/**
 * Сервис управления курсами
 */
public interface CourseService {

    /**
     * Проверить существование курса по идентификатору из LMS
     *
     * @param courseId Идентификатор курса из LMS
     * @return Да / Нет
     * @throws CourseNotFoundException если courseId == null или пустой
     */
    boolean existsByCourseId(String courseId);
}
