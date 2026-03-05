package ru.misis.gamification.service.application.user;

import ru.misis.gamification.model.UserProgressView;

public interface UserProgressApplicationService {

    /**
     * Получить прогресс пользователя по идентификатору из LMS
     *
     * @param userId Идентификатор пользователя из LMS
     * @return Модель прогресса пользователя
     */
    UserProgressView getProgress(String userId);
}
