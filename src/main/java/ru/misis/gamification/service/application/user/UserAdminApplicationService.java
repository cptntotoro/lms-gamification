package ru.misis.gamification.service.application.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.model.UserAdminView;

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
     * @param pageable Параметры пагинации и сортировки
     * @return Страница моделей пользователя для администратора
     */
    Page<UserAdminView> findAll(Pageable pageable);
}
