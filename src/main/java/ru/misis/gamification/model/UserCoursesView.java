package ru.misis.gamification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Полная статистика пользователя (общая + по всем его курсам и группам)
 */
@Builder
@AllArgsConstructor
@Data
public class UserCoursesView {

    /**
     * Идентификатор пользователя из LMS
     */
    private String userId;

    /**
     * Общее количество очков
     */
    private Integer totalPoints;

    /**
     * Уровень
     */
    private Integer level;

    /**
     * Количество очков до следующего уровня
     */
    private Long pointsToNextLevel;

    /**
     * Процент прогресса до следующего уровня (0-100)
     */
    private Double progressPercent;

    /**
     * Список статистики пользователя по курсам и опционально группам
     */
    private List<UserCourseSummary> courses;
}