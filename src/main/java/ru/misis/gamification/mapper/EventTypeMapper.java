package ru.misis.gamification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.misis.gamification.dto.admin.request.EventTypeCreateDto;
import ru.misis.gamification.dto.admin.request.EventTypeUpdateDto;
import ru.misis.gamification.dto.admin.response.EventTypeDto;
import ru.misis.gamification.entity.EventType;

/**
 * Маппер событий
 */
@Mapper(componentModel = "spring")
public interface EventTypeMapper {

    /**
     * Смаппить тип события из LMS в DTO типа события для администратора
     *
     * @param eventType Тип события из LMS
     * @return DTO типа события для администратора
     */
    EventTypeDto eventTypeToEventTypeDto(EventType eventType);

    /**
     * Смаппить DTO события из LMS в событие из LMS
     *
     * @param eventTypeCreateDto DTO создания типа события для LMS
     * @return Событие из LMS
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventType eventTypeCreateDtoToEventType(EventTypeCreateDto eventTypeCreateDto);

    /**
     * Смаппить DTO обновления типа события для LMS в событие из LMS
     *
     * @param eventTypeUpdateDto DTO обновления типа события для LMS
     * @return Тип события из LMS
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "typeCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventType eventTypeUpdateDtoToEventType(EventTypeUpdateDto eventTypeUpdateDto);
}
