package ru.misis.gamification.exception;

/**
 * Исключение, когда пытаемся начислить очки по курсу, на который пользователь не зачислен
 */
public class UserNotEnrolledInCourseException extends RuntimeException {
    public UserNotEnrolledInCourseException(String userId, String courseId) {
        super("Пользователь " + userId + " не зачислен на курс " + courseId);
    }
}