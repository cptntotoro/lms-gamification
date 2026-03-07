package ru.misis.gamification.events;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Событие создания пользователя
 */
@Data
@Builder
@RequiredArgsConstructor
public class UserCreatedEvent {

    /**
     * Идентификатор пользователя из LMS
     */
    private final String userId;

    /**
     * Идентификатор курса из LMS
     */
    private final String courseId;

    /**
     * Идентификатор группы из LMS
     */
    private final String groupId;
}
