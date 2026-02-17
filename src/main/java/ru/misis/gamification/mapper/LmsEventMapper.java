package ru.misis.gamification.mapper;

import org.mapstruct.Mapper;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.model.entity.LmsEvent;

/**
 * Маппер событий из LMS
 */
@Mapper(componentModel = "spring")
public interface LmsEventMapper {

    /**
     * Смаппить запрос на событие из LMS в событие из LMS
     *
     * @param lmsEventRequestDto Запрос на событие из LMS
     * @return Событие из LMS
     */
    LmsEvent lmsEventRequestDtotoLmsEvent(LmsEventRequestDto lmsEventRequestDto);
}
