package ru.misis.gamification.exception;

/**
 * Исключение, выбрасываемое в случае получения дубликата типа события
 */
public class DuplicateEventTypeException extends RuntimeException {
    public DuplicateEventTypeException(String typeCode) {
        super("Тип события с таким кодом уже существует: " + typeCode);
    }
}