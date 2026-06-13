package ru.misis.gamification.service.application.user;

import ru.misis.gamification.dto.user.response.CourseWithEnrollmentDto;
import ru.misis.gamification.dto.user.response.GroupWithMembershipDto;
import ru.misis.gamification.model.UserCoursesView;
import ru.misis.gamification.model.UserStatisticsView;

import java.util.List;

/**
 * Фасадный сервис управления статистикой пользователей
 */
public interface UserStatisticsApplicationService {

    /**
     * Получить статистику пользователя по группе и курсу
     *
     * @param userId   Идентификатор пользователя из LMS
     * @param courseId Идентификатор курса из LMS
     * @param groupId  Идентификатор группы из LMS
     * @return Модель статистики пользователя
     */
    UserStatisticsView getUserStatistics(String userId, String courseId, String groupId);

    /**
     * Получить полную статистику пользователя (общая + по всем его курсам и группам)
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Статистика пользователя общая + по всем его курсам и группам
     */
    UserCoursesView getUserCourses(String userId);

    /**
     * Получить все курсы системы с отметкой, записан ли на них пользователь
     *
     * @param userId Идентификатор пользователя
     * @return Список курсов с флагом enrolled
     */
    List<CourseWithEnrollmentDto> getAllCoursesWithEnrollmentStatus(String userId);

    /**
     * Получить все группы курса с отметкой, состоит ли в них пользователь
     *
     * @param courseId Идентификатор курса
     * @param userId   Идентификатор пользователя
     * @return Список групп с флагом member
     */
    List<GroupWithMembershipDto> getCourseGroupsWithMembership(String courseId, String userId);
}
