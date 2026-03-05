package ru.misis.gamification.model;

import java.util.UUID;

/**
 * Модель зачисления на курс
 *
 * @param userUuid       UUID пользователя
 * @param userId         Идентификатор пользователя из LMS
 * @param courseUuid     UUID курса
 * @param courseId       Идентификатор курса из LMS
 * @param groupUuid      UUID группы
 * @param groupId        Идентификатор группы из LMS
 * @param pointsInCourse Количество заработанных очков на курсе
 */
public record CourseEnrollmentSummary(
        UUID userUuid,
        String userId,
        UUID courseUuid,
        String courseId,
        UUID groupUuid,
        String groupId,
        Integer pointsInCourse
) {
}
