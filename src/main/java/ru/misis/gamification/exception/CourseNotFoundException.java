package ru.misis.gamification.exception;

/**
 * Исключение, когда курс не найден по внешнему ID
 */
public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(String courseId) {
        super("Курс с идентификатором не найден: " + courseId);
    }
}