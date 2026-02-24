package ru.misis.gamification.service.point;

import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.service.point.result.AwardResult;

/**
 * Сервис начисления очков
 * Вся сложная бизнес-логика делегируется в PointsAwardingDomainService
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
