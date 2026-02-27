package ru.misis.gamification.service.analytics;

import ru.misis.gamification.dto.analytics.GroupLeaderboardPageDto;

/**
 * Сервис аналитики и отчётов по геймификации
 */
public interface AnalyticsService {

    /**
     * Получить лидерборд пользователей внутри конкретной группы на курсе с пагинацией
     * <p>
     * Пользователи сортируются по убыванию очков, заработанных именно на данном курсе
     * <p>
     * Если группа или курс не существуют — возвращается пустая страница.
     * Если в группе нет студентов — возвращается страница с totalElements = 0.
     * </p>
     *
     * @param courseId Идентификатор курса из LMS (обязательный)
     * @param groupId  Идентификатор группы из LMS (обязательный)
     * @param page     Номер страницы (0-based, по умолчанию 0)
     * @param size     Размер страницы
     * @return страница лидерборда с метаданными пагинации
     * @throws IllegalArgumentException если courseId или groupId == null
     */
    GroupLeaderboardPageDto getGroupLeaderboard(String courseId, String groupId, int page, int size);
}