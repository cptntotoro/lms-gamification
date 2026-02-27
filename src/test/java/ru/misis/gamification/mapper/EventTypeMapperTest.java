package ru.misis.gamification.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.misis.gamification.dto.admin.request.EventTypeCreateDto;
import ru.misis.gamification.dto.admin.request.EventTypeUpdateDto;
import ru.misis.gamification.dto.admin.response.EventTypeDto;
import ru.misis.gamification.entity.EventType;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventTypeMapperTest {

    private final EventTypeMapper mapper = Mappers.getMapper(EventTypeMapper.class);

    @Test
    void eventTypeToEventTypeDto_mapsAllFieldsCorrectly() {
        EventType entity = EventType.builder()
                .uuid(UUID.randomUUID())
                .typeCode("quiz")
                .displayName("Тест / Квиз")
                .points(80)
                .maxDailyPoints(300)
                .active(true)
                .createdAt(LocalDateTime.of(2026, 2, 10, 14, 30))
                .updatedAt(LocalDateTime.of(2026, 2, 18, 9, 45))
                .build();

        EventTypeDto dto = mapper.eventTypeToEventTypeDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(entity.getUuid());
        assertThat(dto.getTypeCode()).isEqualTo("quiz");
        assertThat(dto.getDisplayName()).isEqualTo("Тест / Квиз");
        assertThat(dto.getPoints()).isEqualTo(80);
        assertThat(dto.getMaxDailyPoints()).isEqualTo(300);
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(dto.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
    }

    @Test
    void eventTypeToEventTypeDto_nullEntity_returnsNull() {
        assertThat(mapper.eventTypeToEventTypeDto(null)).isNull();
    }

    @Test
    void eventTypeCreateDtoToEventType_mapsFieldsAndIgnoresIgnoredOnes() {
        EventTypeCreateDto dto = new EventTypeCreateDto();
        dto.setTypeCode("lab");
        dto.setDisplayName("Лабораторная работа");
        dto.setPoints(150);
        dto.setMaxDailyPoints(500);

        EventType entity = mapper.eventTypeCreateDtoToEventType(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getUuid()).isNull();
        assertThat(entity.getTypeCode()).isEqualTo("lab");
        assertThat(entity.getDisplayName()).isEqualTo("Лабораторная работа");
        assertThat(entity.getPoints()).isEqualTo(150);
        assertThat(entity.getMaxDailyPoints()).isEqualTo(500);
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    void eventTypeCreateDtoToEventType_nullDto_returnsNull() {
        assertThat(mapper.eventTypeCreateDtoToEventType(null)).isNull();
    }

    @Test
    void eventTypeUpdateDtoToEventType_fillsOnlyProvidedFields() {
        EventTypeUpdateDto dto = new EventTypeUpdateDto();
        dto.setDisplayName("Новое название");
        dto.setPoints(150);
        dto.setActive(false);

        EventType result = mapper.eventTypeUpdateDtoToEventType(dto);

        assertThat(result).isNotNull();
        assertThat(result.getDisplayName()).isEqualTo("Новое название");
        assertThat(result.getPoints()).isEqualTo(150);
        assertThat(result.isActive()).isFalse();

        // поля, которые игнорируются или не были переданы
        assertThat(result.getUuid()).isNull();
        assertThat(result.getTypeCode()).isNull();
        assertThat(result.getMaxDailyPoints()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    void eventTypeUpdateDtoToEventType_nullDto_returnsNull() {
        assertThat(mapper.eventTypeUpdateDtoToEventType(null)).isNull();
    }

    @Test
    void eventTypeUpdateDtoToEventType_emptyDto_returnsDefaultObject() {
        EventTypeUpdateDto dto = new EventTypeUpdateDto();

        EventType result = mapper.eventTypeUpdateDtoToEventType(dto);

        assertThat(result).isNotNull();
        assertThat(result.getDisplayName()).isNull();
        assertThat(result.getPoints()).isNull();
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUuid()).isNull();
        assertThat(result.getTypeCode()).isNull();
        assertThat(result.getMaxDailyPoints()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }
}