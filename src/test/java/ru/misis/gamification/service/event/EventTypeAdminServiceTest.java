package ru.misis.gamification.service.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.exception.DuplicateEventTypeException;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.repository.EventTypeRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventTypeAdminServiceTest {

    @Mock
    private EventTypeRepository repository;

    @InjectMocks
    private EventTypeAdminServiceImpl service;

    @Captor
    private ArgumentCaptor<EventType> eventTypeCaptor;

    private EventType testType;

    @BeforeEach
    void setUp() {
        testType = EventType.builder()
                .uuid(UUID.randomUUID())
                .typeCode("quiz")
                .displayName("Тест / Квиз")
                .points(80)
                .maxDailyPoints(300)
                .active(true)
                .build();
    }

    @Test
    void create_uniqueCode_savesAndReturns() {
        when(repository.existsByTypeCode("quiz")).thenReturn(false);
        when(repository.save(any(EventType.class))).thenReturn(testType);

        EventType result = service.create(testType);

        assertThat(result).isEqualTo(testType);
        verify(repository).existsByTypeCode("quiz");
        verify(repository).save(eventTypeCaptor.capture());
        assertThat(eventTypeCaptor.getValue().getTypeCode()).isEqualTo("quiz");
    }

    @Test
    void create_duplicateCode_throwsIllegalArgument() {
        when(repository.existsByTypeCode("quiz")).thenReturn(true);

        assertThatThrownBy(() -> service.create(testType))
                .isInstanceOf(DuplicateEventTypeException.class)
                .hasMessageContaining("уже существует");

        verify(repository).existsByTypeCode("quiz");
        verify(repository, never()).save(any());
    }

    @Test
    void getById_existing_returnsType() {
        UUID id = testType.getUuid();
        when(repository.findById(id)).thenReturn(Optional.of(testType));

        EventType result = service.getById(id);

        assertThat(result).isEqualTo(testType);
        verify(repository).findById(id);
    }

    @Test
    void getById_nonExisting_throwsEntityNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(EventTypeNotFoundException.class);
    }

    @Test
    void update_updatesOnlyProvidedFields() {
        UUID id = testType.getUuid();
        when(repository.findById(id)).thenReturn(Optional.of(testType));

        EventType updated = EventType.builder()
                .displayName("Новый квиз")
                .points(120)
                .active(false)
                .build();

        when(repository.save(any(EventType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventType result = service.update(id, updated);

        assertThat(result.getDisplayName()).isEqualTo("Новый квиз");
        assertThat(result.getPoints()).isEqualTo(120);
        assertThat(result.isActive()).isFalse();
        assertThat(result.getTypeCode()).isEqualTo("quiz"); // не изменился
        assertThat(result.getMaxDailyPoints()).isEqualTo(300); // не передан → сохранён старый

        verify(repository).save(eventTypeCaptor.capture());
        assertThat(eventTypeCaptor.getValue().isActive()).isFalse();
    }

    @Test
    void deactivate_existing_setsActiveFalse() {
        UUID id = testType.getUuid();
        when(repository.findById(id)).thenReturn(Optional.of(testType));

        when(repository.save(any(EventType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deactivate(id);

        verify(repository).save(eventTypeCaptor.capture());
        assertThat(eventTypeCaptor.getValue().isActive()).isFalse();
    }

    @Test
    void deactivate_nonExisting_throwsEntityNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivate(id))
                .isInstanceOf(EventTypeNotFoundException.class);
    }

    @Test
    void findAll_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EventType> page = new PageImpl<>(List.of(testType), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(page);

        Page<EventType> result = service.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(testType);
        verify(repository).findAll(pageable);
    }
}