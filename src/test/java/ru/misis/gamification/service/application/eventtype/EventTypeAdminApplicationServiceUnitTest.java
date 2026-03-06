package ru.misis.gamification.service.application.eventtype;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.mapper.EventTypeMapper;
import ru.misis.gamification.model.EventTypeSummary;
import ru.misis.gamification.service.simple.eventtype.EventTypeAdminService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventTypeAdminApplicationServiceUnitTest {

    @Mock
    private EventTypeAdminService eventTypeAdminService;

    @Mock
    private EventTypeMapper eventTypeMapper;

    @InjectMocks
    private EventTypeAdminApplicationServiceImpl service;

    @Test
    void create_savesAndMaps() {
        EventType input = EventType.builder().typeCode("quiz").build();
        EventType saved = EventType.builder().uuid(UUID.randomUUID()).typeCode("quiz").build();
        EventTypeSummary summary = new EventTypeSummary(saved.getUuid(), "quiz", null, null, null, true, null, null);

        when(eventTypeAdminService.create(input)).thenReturn(saved);
        when(eventTypeMapper.toEventTypeSummary(saved)).thenReturn(summary);

        EventTypeSummary result = service.create(input);

        assertThat(result).isSameAs(summary);
        verify(eventTypeAdminService).create(input);
        verify(eventTypeMapper).toEventTypeSummary(saved);
    }

    @Test
    void getById_findsAndMaps() {
        UUID id = UUID.randomUUID();
        EventType entity = EventType.builder().uuid(id).build();
        EventTypeSummary summary = new EventTypeSummary(id, "lab", null, null, null, true, null, null);

        when(eventTypeAdminService.getById(id)).thenReturn(entity);
        when(eventTypeMapper.toEventTypeSummary(entity)).thenReturn(summary);

        EventTypeSummary result = service.getById(id);

        assertThat(result).isSameAs(summary);
        verify(eventTypeAdminService).getById(id);
        verify(eventTypeMapper).toEventTypeSummary(entity);
    }

    @Test
    void update_updatesAndMaps() {
        UUID id = UUID.randomUUID();
        EventType input = EventType.builder().displayName("New").build();
        EventType updated = EventType.builder().uuid(id).displayName("New").build();
        EventTypeSummary summary = new EventTypeSummary(id, "quiz", "New", null, null, true, null, null);

        when(eventTypeAdminService.update(eq(id), eq(input))).thenReturn(updated);
        when(eventTypeMapper.toEventTypeSummary(updated)).thenReturn(summary);

        EventTypeSummary result = service.update(id, input);

        assertThat(result).isSameAs(summary);
        verify(eventTypeAdminService).update(id, input);
        verify(eventTypeMapper).toEventTypeSummary(updated);
    }

    @Test
    void deactivate_callsDeactivate() {
        UUID id = UUID.randomUUID();

        service.deactivate(id);

        verify(eventTypeAdminService).deactivate(id);
        verifyNoMoreInteractions(eventTypeAdminService, eventTypeMapper);
    }

    @Test
    void findAll_mapsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        EventType e1 = EventType.builder().uuid(UUID.randomUUID()).build();
        EventType e2 = EventType.builder().uuid(UUID.randomUUID()).build();
        Page<EventType> page = new PageImpl<>(List.of(e1, e2), pageable, 2);

        EventTypeSummary s1 = new EventTypeSummary(e1.getUuid(), null, null, null, null, true, null, null);
        EventTypeSummary s2 = new EventTypeSummary(e2.getUuid(), null, null, null, null, true, null, null);

        when(eventTypeAdminService.findAll(pageable)).thenReturn(page);
        when(eventTypeMapper.toEventTypeSummary(e1)).thenReturn(s1);
        when(eventTypeMapper.toEventTypeSummary(e2)).thenReturn(s2);

        Page<EventTypeSummary> result = service.findAll(pageable);

        assertThat(result.getContent()).containsExactly(s1, s2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(eventTypeAdminService).findAll(pageable);
        verify(eventTypeMapper).toEventTypeSummary(e1);
        verify(eventTypeMapper).toEventTypeSummary(e2);
    }

    @Test
    void findAll_emptyPage_returnsEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EventType> emptyPage = Page.empty(pageable);

        when(eventTypeAdminService.findAll(pageable)).thenReturn(emptyPage);

        Page<EventTypeSummary> result = service.findAll(pageable);

        assertThat(result.isEmpty()).isTrue();
        verify(eventTypeAdminService).findAll(pageable);
        verifyNoInteractions(eventTypeMapper);
    }
}