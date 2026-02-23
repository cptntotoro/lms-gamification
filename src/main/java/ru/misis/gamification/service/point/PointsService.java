package ru.misis.gamification.service.point;

import ru.misis.gamification.dto.AwardResult;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;

/**
 * Сервис начисления очков
 */
public interface PointsService {

    /**
     * Начислить очки пользователю за событие от LMS
     *
     * @param request DTO события из LMS
     * @return Результат начисления (успех, дубликат, отклонение и т.д.)
     */
    AwardResult awardPoints(LmsEventRequestDto request);
}
