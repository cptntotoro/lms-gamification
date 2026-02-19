package ru.misis.gamification.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.exception.UserNotFoundException;

/**
 * Сервис управления пользователями для администратора
 */
public interface UserAdminService {

    /**
     * Поиск пользователя по идентификатору пользователя из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return DTO пользователя для администратора
     * @throws UserNotFoundException если пользователь не найден
     */
    UserAdminDto findByUserId(String userId);

    /**
     * Получение списка пользователей с пагинацией (для будущего лидерборда/админки)
     *
     * @param pageable Параметры пагинации и сортировки
     * @return Страница DTO пользователей для администратора
     */
    Page<UserAdminDto> findAll(Pageable pageable);
}
