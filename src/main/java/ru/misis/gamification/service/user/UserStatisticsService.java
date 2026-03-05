//package ru.misis.gamification.service.user;
//
//import jakarta.annotation.Nullable;
//import ru.misis.gamification.dto.user.response.UserStatisticsDto;
//import ru.misis.gamification.exception.UserNotEnrolledInCourseException;
//
//public interface UserStatisticsService {
//
//    /**
//     * Получить данные пользователя в контексте курса и (опционально) группы
//     *
//     * @param userId   Идентификатор пользователя из LMS
//     * @param courseId Идентификатор курса из LMS
//     * @param groupId  Идентификатор группы из LMS
//     * @return DTO с глобальными и контекстными метриками
//     * @throws UserNotEnrolledInCourseException если пользователь не зачислен на курс
//     */
//    UserStatisticsDto getUserStatisticsGlobalAndByCourseAndGroup(
//            String courseId,
//            @Nullable String groupId,
//            String userId
//    );
//}
