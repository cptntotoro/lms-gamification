package ru.misis.gamification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.dto.user.response.UserDto;
import ru.misis.gamification.dto.user.response.UserGlobalCourseGroupDto;
import ru.misis.gamification.dto.user.response.UserStatisticsDto;
import ru.misis.gamification.model.UserAdminView;
import ru.misis.gamification.model.UserProgressView;
import ru.misis.gamification.model.UserStatisticsView;

/**
 * Маппер пользователей
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Смаппить модель прогресса пользователя в DTO пользователя
     *
     * @param view Модель прогресса пользователя
     * @return DTO пользователя
     */
    UserDto toUserDto(UserProgressView view);

    /**
     * Смаппить модель пользователя для админ-панели в соответствующий DTO
     *
     * @param view Модель пользователя для админ-панели
     * @return DTO пользователя для администратора
     */
    UserAdminDto toUserAdminDto(UserAdminView view);

    /**
     * Смаппить модель статистики пользователя по курсу/группе в DTO
     *
     * @param view Модель статистики пользователя
     * @return DTO пользователя в контексте курса и (опционально) группы
     */
    UserStatisticsDto toUserStatisticsDto(UserStatisticsView view);

    /**
     * Смаппить модель прогресса пользователя в DTO
     *
     * @param progress Модель прогресса пользователя
     * @return DTO пользователя с глобальными данными + опционально по курсу/группе
     */
    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "groupId", ignore = true)
    @Mapping(target = "pointsInCourse", ignore = true)
    @Mapping(target = "rankInCourse", ignore = true)
    @Mapping(target = "rankInGroup", ignore = true)
    UserGlobalCourseGroupDto toUserGlobalCourseGroupDto(UserProgressView progress);
}
