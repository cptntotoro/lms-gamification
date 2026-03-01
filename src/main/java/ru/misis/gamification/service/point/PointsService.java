package ru.misis.gamification.service.point;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.service.point.result.AwardResult;

/**
 * Сервис начисления очков
 */
public interface PointsService {

    /**
     * Начислить очки пользователю за событие от LMS
     *
     * @param lmsEventRequestDto DTO события из LMS
     * @return Результат начисления (успех, дубликат, отклонение и т.д.)
     * @throws ConstraintViolationException если lmsEventRequestDto == null
     */
    AwardResult awardPoints(@NotNull(message = "{request.required}") LmsEventRequestDto lmsEventRequestDto);
}
