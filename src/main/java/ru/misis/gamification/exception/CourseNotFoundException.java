package ru.misis.gamification.exception;

import java.util.UUID;

/**
 * Исключение, когда курс не найден по внешнему ID
 */
public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(String courseId) {
        super("Курс с идентификатором не найден: " + courseId);
    }

    public CourseNotFoundException(UUID uuid) {
        super("Курс с uuid не найден: " + uuid);
    }
}