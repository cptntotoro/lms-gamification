package ru.misis.gamification.service.application.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.model.UserAdminView;

/**
 * Фасадный сервис управления пользователями для администратора
 */
public interface UserAdminApplicationService {

    /**
     * Получить модель пользователя для администратора по его идентификатору из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Модель пользователя для администратора
     */
    UserAdminView findByUserId(String userId);

    /**
     * Получить страницу моделей пользователя для администратора
     *
     * @param courseId Идентификатор курса из LMS
     * @param groupId  Идентификатор группы из LMS
     * @param pageable Параметры пагинации и сортировки
     * @return Страница моделей пользователя для администратора
     */
    Page<UserAdminView> findAll(String courseId, String groupId, Pageable pageable);
}
