package ru.misis.gamification.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.misis.gamification.dto.admin.request.EventTypeCreateDto;
import ru.misis.gamification.dto.admin.request.EventTypeUpdateDto;
import ru.misis.gamification.dto.admin.response.EventTypeDto;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.model.EventTypeSummary;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventTypeMapperTest {

    private final EventTypeMapper mapper = Mappers.getMapper(EventTypeMapper.class);

    @Test
    void toEventTypeDto_mapsAllFieldsCorrectly() {
        EventTypeSummary summary = new EventTypeSummary(
                UUID.randomUUID(),
                "quiz",
                "Тест / Квиз",
                80,
                300,
                true,
                LocalDateTime.of(2026, 2, 10, 14, 30),
                LocalDateTime.of(2026, 2, 18, 9, 45)
        );

        EventTypeDto dto = mapper.toEventTypeDto(summary);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(summary.uuid());
        assertThat(dto.getTypeCode()).isEqualTo("quiz");
        assertThat(dto.getDisplayName()).isEqualTo("Тест / Квиз");
        assertThat(dto.getPoints()).isEqualTo(80);
        assertThat(dto.getMaxDailyPoints()).isEqualTo(300);
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.getCreatedAt()).isEqualTo(summary.createdAt());
        assertThat(dto.getUpdatedAt()).isEqualTo(summary.updatedAt());
    }

    @Test
    void toEventTypeDto_nullSummary_returnsNull() {
        assertThat(mapper.toEventTypeDto(null)).isNull();
    }

    @Test
    void toEventTypeSummary_mapsAllFieldsCorrectly() {
        EventType entity = EventType.builder()
                .uuid(UUID.randomUUID())
                .typeCode("lab")
                .displayName("Лабораторная работа")
                .points(150)
                .maxDailyPoints(500)
                .active(false)
                .createdAt(LocalDateTime.of(2026, 1, 15, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 20, 15, 30))
                .build();

        EventTypeSummary summary = mapper.toEventTypeSummary(entity);

        assertThat(summary).isNotNull();
        assertThat(summary.uuid()).isEqualTo(entity.getUuid());
        assertThat(summary.typeCode()).isEqualTo("lab");
        assertThat(summary.displayName()).isEqualTo("Лабораторная работа");
        assertThat(summary.points()).isEqualTo(150);
        assertThat(summary.maxDailyPoints()).isEqualTo(500);
        assertThat(summary.active()).isFalse();
        assertThat(summary.createdAt()).isEqualTo(entity.getCreatedAt());
        assertThat(summary.updatedAt()).isEqualTo(entity.getUpdatedAt());
    }

    @Test
    void toEventTypeSummary_nullEntity_returnsNull() {
        assertThat(mapper.toEventTypeSummary(null)).isNull();
    }

    @Test
    void eventTypeCreateDtoToEventType_mapsFieldsAndIgnoresIgnoredOnes() {
        EventTypeCreateDto dto = new EventTypeCreateDto();
        dto.setTypeCode("homework");
        dto.setDisplayName("Домашняя работа");
        dto.setPoints(50);
        dto.setMaxDailyPoints(200);

        EventType entity = mapper.eventTypeCreateDtoToEventType(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getUuid()).isNull();
        assertThat(entity.getTypeCode()).isEqualTo("homework");
        assertThat(entity.getDisplayName()).isEqualTo("Домашняя работа");
        assertThat(entity.getPoints()).isEqualTo(50);
        assertThat(entity.getMaxDailyPoints()).isEqualTo(200);
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
        dto.setDisplayName("Новое название квиза");
        dto.setPoints(100);
        dto.setActive(false);

        EventType result = mapper.eventTypeUpdateDtoToEventType(dto);

        assertThat(result).isNotNull();
        assertThat(result.getDisplayName()).isEqualTo("Новое название квиза");
        assertThat(result.getPoints()).isEqualTo(100);
        assertThat(result.isActive()).isFalse();

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