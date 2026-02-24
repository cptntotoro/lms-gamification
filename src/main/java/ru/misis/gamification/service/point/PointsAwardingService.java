package ru.misis.gamification.service.point;

import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.service.point.result.AwardResult;

/**
 * Сервис бизнес-логики начисления очков за событие из LMS
 * <p>
 * Основная ответственность — выполнение всей последовательности проверки и начисления очков:
 * <ul>
 *     <li>проверка на дубликат события</li>
 *     <li>получение и валидация активного типа события</li>
 *     <li>проверка дневного лимита по типу</li>
 *     <li>создание транзакции</li>
 *     <li>обновление уровня и общего количества очков пользователя</li>
 * </ul>
 * </p>
 */
public interface PointsAwardingService {

    /**
     * Выполняет начисление очков за событие из LMS
     *
     * @param request DTO события из LMS
     * @return Внутренний результат операции начисления очков (никогда null)
     * @throws IllegalArgumentException если request null или содержит пустые обязательные поля
     */
    AwardResult awardPoints(LmsEventRequestDto request);
}