package ru.misis.gamification.service.user;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
     * @throws UserNotFoundException        если пользователь не найден
     * @throws ConstraintViolationException userId == null или пустая строка
     */
    UserAdminDto findByUserId(@NotBlank(message = "{user.id.required}") String userId);

    /**
     * Получение списка пользователей с пагинацией (для будущего лидерборда/админки)
     *
     * @param pageable Параметры пагинации и сортировки
     * @return Страница DTO пользователей для администратора
     * @throws ConstraintViolationException если pageable == null
     */
    Page<UserAdminDto> findAll(@NotNull(message = "{pageable.required}") Pageable pageable);
}
