package ru.misis.gamification.exception;

/**
 * Исключение, когда зачисление на курс (связь пользователь — курс) не найдено
 */
public class UserCourseEnrollmentNotFoundException extends RuntimeException {
    public UserCourseEnrollmentNotFoundException(String userId, String courseId) {
        super("Пользователь " + userId + " не зачислен на курс " + courseId);
    }
}