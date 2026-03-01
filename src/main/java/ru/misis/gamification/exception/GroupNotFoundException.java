package ru.misis.gamification.exception;

import java.util.UUID;

/**
 * Исключение, когда группа не найдена внутри курса по внешнему ID
 */
public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException(String groupId, String courseId) {
        super("Группа с идентификатором " + groupId + " не найдена в курсе " + courseId);
    }

    public GroupNotFoundException(UUID groupUuid) {
        super("Группа с UUID " + groupUuid + " не найдена");
    }
}