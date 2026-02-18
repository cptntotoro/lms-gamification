package ru.misis.gamification.mapper;

import org.mapstruct.Mapper;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.dto.web.response.UserDto;
import ru.misis.gamification.model.entity.User;

/**
 * Маппер пользователей
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Смаппить сущность пользователя в DTO для ответа администратору
     *
     * @param user Пользователь
     * @return DTO пользователя для администратора
     */
    UserAdminDto userToUserAdminDto(User user);

    /**
     * Смаппить сущность пользователя в DTO пользователя для виджета
     *
     * @param user Пользователь
     * @return DTO пользователя для виджета
     */
    UserDto userToUserDto(User user);
}
