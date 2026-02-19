package ru.misis.gamification.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.gamification.dto.admin.request.EventTypeCreateDto;
import ru.misis.gamification.dto.admin.request.EventTypeUpdateDto;
import ru.misis.gamification.dto.admin.response.EventTypeDto;
import ru.misis.gamification.mapper.EventTypeMapper;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.service.event.admin.EventTypeAdminService;

import java.util.UUID;

//@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/event-types")
@RequiredArgsConstructor
@Tag(name = "Admin - Типы событий", description = "CRUD-операции с типами событий (настройка начисления очков за действия в LMS)")
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
    @Operation(
            summary = "Создать новый тип события",
            description = "Добавляет новый тип события в систему. Код типа должен быть уникальным."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Тип события успешно создан",
                    content = @Content(schema = @Schema(implementation = EventTypeDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные (валидация DTO)"),
            @ApiResponse(responseCode = "409", description = "Тип события с таким typeCode уже существует")
    })
    public ResponseEntity<EventTypeDto> create(@Valid @RequestBody EventTypeCreateDto dto) {
        EventType entity = eventTypeMapper.eventTypeCreateDtoToEventType(dto);
        EventType saved = eventTypeAdminService.create(entity);
        return ResponseEntity.ok(eventTypeMapper.eventTypeToEventTypeDto(saved));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить тип события по внутреннему ID",
            description = "Возвращает полную информацию о типе события по его UUID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Тип события найден",
                    content = @Content(schema = @Schema(implementation = EventTypeDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Тип события не найден")
    })
    public ResponseEntity<EventTypeDto> getById(@PathVariable UUID id) {
        EventType type = eventTypeAdminService.getById(id);
        return ResponseEntity.ok(eventTypeMapper.eventTypeToEventTypeDto(type));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить существующий тип события",
            description = "Частичное обновление типа события. Поле typeCode изменить нельзя."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Тип события успешно обновлён",
                    content = @Content(schema = @Schema(implementation = EventTypeDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "404", description = "Тип события не найден")
    })
    public ResponseEntity<EventTypeDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody EventTypeUpdateDto dto) {

        EventType entity = eventTypeMapper.eventTypeUpdateDtoToEventType(dto);
        EventType updated = eventTypeAdminService.update(id, entity);
        return ResponseEntity.ok(eventTypeMapper.eventTypeToEventTypeDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Деактивировать тип события",
            description = "Устанавливает флаг active = false. Тип события остаётся в базе, но становится недоступным для начисления очков."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Тип события успешно деактивирован"),
            @ApiResponse(responseCode = "404", description = "Тип события не найден")
    })
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        eventTypeAdminService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "Получить список всех типов событий с пагинацией",
            description = "Возвращает страницу типов событий с возможностью сортировки и фильтрации"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список типов событий успешно получен",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    public ResponseEntity<Page<EventTypeDto>> getAll(Pageable pageable) {
        Page<EventType> page = eventTypeAdminService.findAll(pageable);
        return ResponseEntity.ok(page.map(eventTypeMapper::eventTypeToEventTypeDto));
    }
}