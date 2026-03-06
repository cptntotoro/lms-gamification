package ru.misis.gamification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.misis.gamification.dto.admin.request.EventTypeCreateDto;
import ru.misis.gamification.dto.admin.request.EventTypeUpdateDto;
import ru.misis.gamification.dto.admin.response.EventTypeDto;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.model.EventTypeSummary;

/**
 * Маппер типов событий
 */
@Mapper(componentModel = "spring")
public interface EventTypeMapper {

    /**
     * Смаппить модель типа события в DTO типа события для администратора
     *
     * @param summary Модель типа события
     * @return DTO типа события для администратора
     */
    EventTypeDto toEventTypeDto(EventTypeSummary summary);

    /**
     * Смаппить тип события в модель типа события
     *
     * @param eventType Тип события
     * @return Модель типа события
     */
    EventTypeSummary toEventTypeSummary(EventType eventType);

    /**
     * Смаппить DTO создания типа события в тип события
     *
     * @param eventTypeCreateDto DTO создания типа события
     * @return Тип события
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventType eventTypeCreateDtoToEventType(EventTypeCreateDto eventTypeCreateDto);

    /**
     * Смаппить DTO обновления типа события в тип события
     *
     * @param eventTypeUpdateDto DTO обновления типа события
     * @return Тип события
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "typeCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventType eventTypeUpdateDtoToEventType(EventTypeUpdateDto eventTypeUpdateDto);
}
