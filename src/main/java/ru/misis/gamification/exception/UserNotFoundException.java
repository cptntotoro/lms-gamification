package ru.misis.gamification.exception;

/**
 * Исключение, выбрасываемое в случае обращения к несуществующему пользователю
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String userId) {
        super("Пользователь с таким userId не найден: " + userId);
    }
}