package ru.misis.gamification.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.misis.gamification.dto.admin.request.EventTypeCreateDto;
import ru.misis.gamification.dto.admin.request.EventTypeUpdateDto;
import ru.misis.gamification.dto.admin.response.EventTypeDto;
import ru.misis.gamification.mapper.EventTypeMapper;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.service.event.admin.EventTypeAdminService;

import java.util.UUID;

//@Tag(name = "Admin - Типы событий", description = "Управление типами событий (начисление очков)")
//@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/event-types")
@RequiredArgsConstructor
public class EventTypeAdminController {

    /**
     * Сервис для работы с типами событий для администратора
     */
    private final EventTypeAdminService eventTypeAdminService;

    /**
     * Маппер событий
     */
    private final EventTypeMapper eventTypeMapper;

    @PostMapping
//    @Operation(summary = "Создать новый тип события")
    public ResponseEntity<EventTypeDto> create(@Valid @RequestBody EventTypeCreateDto dto) {
        EventType entity = eventTypeMapper.eventTypeCreateDtoToEventType(dto);
        EventType saved = eventTypeAdminService.create(entity);
        return ResponseEntity.ok(eventTypeMapper.eventTypeToEventTypeDto(saved));
    }

    @GetMapping("/{id}")
//    @Operation(summary = "Получить тип события по ID")
    public ResponseEntity<EventTypeDto> getById(@PathVariable UUID id) {
        EventType type = eventTypeAdminService.getById(id);
        return ResponseEntity.ok(eventTypeMapper.eventTypeToEventTypeDto(type));
    }

    @PutMapping("/{id}")
//    @Operation(summary = "Обновить тип события")
    public ResponseEntity<EventTypeDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody EventTypeUpdateDto dto) {

        EventType entity = eventTypeMapper.eventTypeUpdateDtoToEventType(dto);
        EventType updated = eventTypeAdminService.update(id, entity);
        return ResponseEntity.ok(eventTypeMapper.eventTypeToEventTypeDto(updated));
    }

    @DeleteMapping("/{id}")
//    @Operation(summary = "Деактивировать тип события")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        eventTypeAdminService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
//    @Operation(summary = "Получить все типы событий (с пагинацией)")
    public ResponseEntity<Page<EventTypeDto>> getAll(Pageable pageable) {
        Page<EventType> page = eventTypeAdminService.findAll(pageable);
        return ResponseEntity.ok(page.map(eventTypeMapper::eventTypeToEventTypeDto));
    }
}