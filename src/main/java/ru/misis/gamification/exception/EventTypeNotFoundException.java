package ru.misis.gamification.exception;

import java.util.UUID;

/**
 * Исключение, выбрасываемое в случае обращения к несуществующему типу события
 */
public class EventTypeNotFoundException extends RuntimeException {
    public EventTypeNotFoundException(UUID uuid) {
        super("Тип события не найден: " + uuid);
    }

    public EventTypeNotFoundException(String message) {
        super(message);
    }
}