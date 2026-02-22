package ru.misis.gamification.exception;

/**
 * Исключение, выбрасываемое в случае получения дубликата события
 */
public class InvalidEventTypeException extends RuntimeException {
    public InvalidEventTypeException(String eventId) {
        super("Событие с таким eventId уже было обработано: " + eventId);
    }
}