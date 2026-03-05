//package ru.misis.gamification.service.analytics;
//
//import jakarta.annotation.Nullable;
//import jakarta.validation.ConstraintViolationException;
//import jakarta.validation.constraints.Max;
//import jakarta.validation.constraints.Min;
//import jakarta.validation.constraints.NotBlank;
//import ru.misis.gamification.dto.analytics.UserCourseGroupLeaderboardDto;
//
/// **
// * Сервис аналитики и отчётов по геймификации
// */
//public interface AnalyticsService {
//
//    /**
//     * Получить лидерборд курса для конкретного студента (включая своё место даже вне топа)
//     * <p>
//     * Возвращает топ-N студентов курса + запись текущего пользователя (если зачислен),
//     * помеченную флагом {@code isCurrentUser = true}.
//     * </p>
//     *
//     * @param courseId      Идентификатор курса из LMS
//     * @param groupId       Идентификатор группы из LMS (опционально, null = весь курс)
//     * @param page          Номер страницы (0-based)
//     * @param size          Размер страницы
//     * @param currentUserId Идентификатор текущего пользователя из LMS
//     * @return DTO с лидербордом по курсу для студента {@link UserCourseGroupLeaderboardDto}
//     * @throws ConstraintViolationException если courseId или currentUserId некорректны
//     */
//    UserCourseGroupLeaderboardDto getCourseLeaderboardForUser(
//            @NotBlank(message = "{course.id.required}") String courseId,
//            @Nullable String groupId,
//            @Min(value = 0, message = "{page.non-negative}") int page,
//            @Min(value = 1, message = "{size.positive}") @Max(value = 100, message = "{size.too-large}") int size,
//            @NotBlank(message = "{user.id.required}") String currentUserId);
//}