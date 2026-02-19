package ru.misis.gamification.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.model.entity.LmsEvent;

import static org.assertj.core.api.Assertions.assertThat;

class LmsEventMapperTest {

    private final LmsEventMapper mapper = Mappers.getMapper(LmsEventMapper.class);

    @Test
    void lmsEventRequestDtoToLmsEvent_shouldMapAllFields() {
        LmsEventRequestDto dto = LmsEventRequestDto.builder()
                .userId("lms-user-12345")
                .eventId("event-uuid-abcde-123")
                .pointsEarned(150)
                .build();

        LmsEvent entity = mapper.lmsEventRequestDtotoLmsEvent(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getUserId()).isEqualTo("lms-user-12345");
        assertThat(entity.getEventId()).isEqualTo("event-uuid-abcde-123");
        assertThat(entity.getPointsEarned()).isEqualTo(150);
    }

    @Test
    void lmsEventRequestDtoToLmsEvent_withNullFields_shouldMapNulls() {
        LmsEventRequestDto dto = LmsEventRequestDto.builder()
                .userId(null)
                .eventId("event-001")
                .pointsEarned(null)
                .build();

        LmsEvent entity = mapper.lmsEventRequestDtotoLmsEvent(dto);

        assertThat(entity.getUserId()).isNull();
        assertThat(entity.getEventId()).isEqualTo("event-001");
        assertThat(entity.getPointsEarned()).isNull();
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        LmsEvent result = mapper.lmsEventRequestDtotoLmsEvent(null);
        assertThat(result).isNull();
    }

    @Test
    void shouldHandlePartiallyFilledDto() {
        LmsEventRequestDto dto = LmsEventRequestDto.builder()
                .userId("u-999")
                .eventId(null)
                .pointsEarned(0)
                .build();

        LmsEvent entity = mapper.lmsEventRequestDtotoLmsEvent(dto);

        assertThat(entity.getUserId()).isEqualTo("u-999");
        assertThat(entity.getEventId()).isNull();
        assertThat(entity.getPointsEarned()).isZero();
    }
}