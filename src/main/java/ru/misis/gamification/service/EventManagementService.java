package ru.misis.gamification.service;

import ru.misis.gamification.dto.lms.response.LmsEventResponsetDto;
import ru.misis.gamification.model.entity.LmsEvent;

/**
 * Сервис обработки событий от LMS
 */
public interface EventManagementService {

    /**
     * Обработать событие из LMS
     * <p>
     * Важно: сначала сохраняется транзакция (чтобы при любом сбое пользователь не изменился),
     * затем обновляется профиль пользователя.
     *
     * @param lmsEvent Событие из LMS
     * @return Ответ на событие из LMS
     */
    LmsEventResponsetDto process(LmsEvent lmsEvent);
}
