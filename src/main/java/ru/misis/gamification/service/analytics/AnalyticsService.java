package ru.misis.gamification.service.analytics;

import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import ru.misis.gamification.dto.analytics.GroupLeaderboardPageDto;
import ru.misis.gamification.dto.analytics.UserCourseGroupLeaderboard;

/**
 * Сервис аналитики и отчётов по геймификации
 */
public interface AnalyticsService {

    /**
     * Получить лидерборд пользователей внутри конкретной группы на курсе с пагинацией
     * <p>
     * Лидерборд формируется по убыванию количества очков, заработанных именно на данном курсе
     * (поле {@code totalPointsInCourse} в сущности {@code UserCourseEnrollment}).
     * </p>
     * Если группа или курс не существуют — возвращается пустая страница (totalElements = 0).
     * Если в группе нет студентов — также возвращается страница (totalElements = 0).
     * </p>
     *
     * @param courseId Идентификатор курса из LMS
     * @param groupId  Идентификатор группы из LMS
     * @param page     Номер страницы (0-based, по умолчанию 0)
     * @param size     Размер страницы
     * @return Страница лидерборда с метаданными пагинации {@link GroupLeaderboardPageDto}
     * @throws ConstraintViolationException если:
     *                                      <ul>
     *                                       <li>courseId == null или пустая строка</li>
     *                                       <li>groupId == null или пустая строка</li>
     *                                       <li>page < 0</li>
     *                                       <li>size < 1 или size > 100</li>
     *                                      </ul>
     */
    GroupLeaderboardPageDto getGroupLeaderboard(@NotBlank(message = "{course.id.required}") String courseId,
                                                @Nullable String groupId,
                                                @Min(value = 0, message = "{page.non-negative}") int page,
                                                @Min(value = 1, message = "{size.positive}") @Max(value = 100, message = "{size.too-large}") int size);

    /**
     * Получить лидерборд курса для конкретного студента (включая своё место даже вне топа)
     * <p>
     * Возвращает топ-N студентов курса + запись текущего пользователя (если зачислен),
     * помеченную флагом {@code isCurrentUser = true}.
     * </p>
     *
     * @param courseId      Идентификатор курса из LMS
     * @param groupId       Идентификатор группы из LMS (опционально, null = весь курс)
     * @param page          Номер страницы (0-based)
     * @param size          Размер страницы
     * @param currentUserId Идентификатор текущего пользователя из LMS
     * @return DTO с лидербордом по курсу для студента {@link UserCourseGroupLeaderboard}
     * @throws ConstraintViolationException если courseId или currentUserId некорректны
     */
    UserCourseGroupLeaderboard getCourseLeaderboardForUser(
            @NotBlank(message = "{course.id.required}") String courseId,
            @Nullable String groupId,
            @Min(value = 0, message = "{page.non-negative}") int page,
            @Min(value = 1, message = "{size.positive}") @Max(value = 100, message = "{size.too-large}") int size,
            @NotBlank(message = "{user.id.required}") String currentUserId);
}