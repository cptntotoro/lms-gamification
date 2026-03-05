package ru.misis.gamification.model;

/**
 * Модель статистики пользователя
 *
 * @param userId                  Идентификатор пользователя из LMS
 * @param globalLevel             Общий уровень пользователя
 * @param totalPoints             Общее количество накопленных очков
 * @param courseId                Идентификатор курса из LMS
 * @param groupId                 Идентификатор группы из LMS
 * @param pointsInCourse          Количество заработанных очков на курсе
 * @param rankInCourse            Позиция в лидерборде по курсу
 * @param rankInGroup             Позиция в лидерборде по группе
 * @param pointsToNextGlobalLevel Количество очков до следующего общего уровня
 * @param progressPercent         Процент заполнения текущего уровня
 */
public record UserStatisticsView(
        String userId,
        Integer globalLevel,
        Integer totalPoints,
        String courseId,
        String groupId,
        Integer pointsInCourse,
        Long rankInCourse,
        Long rankInGroup,
        Long pointsToNextGlobalLevel,
        Double progressPercent
) {
}
