package ru.misis.gamification.service.event;

import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.dto.lms.response.LmsEventResponsetDto;

/**
 * Сервис обработки событий от LMS
 */
public interface EventManagementService {

    /**
     * Обработать входящее событие от LMS
     * <p>
     * Алгоритм:
     * 1. Валидация входных данных
     * 2. Получение активного типа события по коду
     * 3. Проверка дневного лимита начисления по типу
     * 4. Создание/получение пользователя
     * 5. Создание и сохранение транзакции (с защитой от дублей)
     * 6. Обновление общего количества очков и уровня пользователя
     * 7. Формирование ответа
     *
     * @param lmsEventRequestDto DTO события из LMS
     * @return DTO ответа LMS-системе на обработанное событие
     */
    LmsEventResponsetDto process(LmsEventRequestDto lmsEventRequestDto);
}
