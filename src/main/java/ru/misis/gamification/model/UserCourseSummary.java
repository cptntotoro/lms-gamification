package ru.misis.gamification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель статистики пользователя по курсу и опционально группе
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCourseSummary {

    /**
     * Идентификатор курса из LMS
     */
    private String courseId;

    /**
     * UUID курса
     */
    private UUID courseUuid;

    /**
     * Название курса
     */
    private String displayName;

    /**
     * Идентификатор группы из LMS
     */
    private String groupId;

    /**
     * UUID группы
     */
    private UUID groupUuid;

    /**
     * Количество очков на курсе
     */
    private Integer totalPointsInCourse;

    /**
     * Дата записи на курс
     */
    private LocalDateTime enrolledAt;

    /**
     * Дата завершения курса (опционально)
     */
    private LocalDateTime completedAt;
}