package ru.misis.gamification.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.misis.gamification.model.UserCourseSummary;

import java.util.List;

/**
 * DTO полной статистики пользователя (общая + по всем его курсам и группам)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO полной статистики пользователя (общая + по всем его курсам и группам)")
public class UserCoursesResponseDto {

    /**
     * Идентификатор пользователя из LMS
     */
    private String userId;

    /**
     * Сумма очков
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

    /**
     * Общее количество курсов
     */
    private int totalCourses;
}