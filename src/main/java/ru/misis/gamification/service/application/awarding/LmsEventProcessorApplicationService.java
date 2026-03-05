package ru.misis.gamification.service.application.awarding;

import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.dto.lms.response.LmsEventResponseDto;

public interface LmsEventProcessorApplicationService {

    /**
     * Обработать событие из LMS
     *
     * @param request DTO события из LMS
     * @return DTO ответа LMS-системе на обработанное событие
     */
    LmsEventResponseDto process(LmsEventRequestDto request);
}
