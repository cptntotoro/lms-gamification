package ru.misis.gamification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Модель строки лидерборда
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryView {

    /**
     * UUID пользователя
     */
    private UUID userUuid;

    /**
     * Идентификатор пользователя из LMS
     */
    private String userId;

    /**
     * Количество очков, набранных пользователем именно на данном курсе
     */
    private Integer pointsInCourse;

    /**
     * Глобальный уровень пользователя
     */
    private Integer globalLevel;

    /**
     * Позиция (ранг) пользователя в текущем лидерборде
     */
    private Long rank;

    /**
     * Флаг, указывающий, является ли данный участник текущим авторизованным пользователем
     * <p>
     * Используется в персонализированном лидерборде для выделения позиции текущего
     * пользователя (например, подсветка строки, отдельный блок "Ваше место").
     * По умолчанию false. Устанавливается в true только для записи текущего пользователя.
     * </p>
     */
    private Boolean isCurrentUser;

    /**
     * Конструктор для создания строки лидерборда без указания флага текущего пользователя.
     * <p>
     * Используется при формировании топа лидерборда, когда флаг isCurrentUser
     * не нужен или всегда false.
     * </p>
     *
     * @param userUuid       UUID пользователя
     * @param userId         Идентификатор пользователя из LMS
     * @param pointsInCourse Количество очков, набранных пользователем именно на данном курсе
     * @param globalLevel    Глобальный уровень пользователя
     * @param rank           Позиция (ранг) пользователя в текущем лидерборде
     */
    public LeaderboardEntryView(UUID userUuid, String userId, Integer pointsInCourse,
                                Integer globalLevel, Long rank) {
        this(userUuid, userId, pointsInCourse, globalLevel, rank, false);
    }
}