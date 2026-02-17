package ru.misis.gamification.exception;

/**
 * Исключение, выбрасываемое в случае получения дубликата события
 */
public class DuplicateEventException extends RuntimeException {
    public DuplicateEventException(String eventId) {
        super("Событие с таким eventId уже было обработано: " + eventId);
    }
}