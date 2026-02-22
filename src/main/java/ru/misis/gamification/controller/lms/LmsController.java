package ru.misis.gamification.controller.lms;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.dto.lms.response.LmsEventResponsetDto;
import ru.misis.gamification.service.event.EventManagementService;

/**
 * Контроллер для приема событий от LMS
 */
@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "LMS Events", description = "Приём и обработка событий от LMS-платформы")
public class LmsController {

    /**
     * Сервис обработки событий от LMS
     */
    private final EventManagementService eventManagementService;

    @PostMapping
    @Operation(
            summary = "Обработать событие от LMS",
            description = "Принимает событие (например, выполнение задания) и начисляет очки пользователю"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Событие успешно обработано",
                    content = @Content(schema = @Schema(implementation = LmsEventResponsetDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные события"),
            @ApiResponse(responseCode = "409", description = "Событие уже было обработано (дубликат)"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<LmsEventResponsetDto> processUserEvent(@RequestBody @Valid LmsEventRequestDto lmsEventRequestDto) {
        log.info("Получен запрос от LMS: userId={}, eventId={}, eventType={}",
                lmsEventRequestDto.getUserId(), lmsEventRequestDto.getEventId(), lmsEventRequestDto.getEventType());

        LmsEventResponsetDto response = eventManagementService.process(lmsEventRequestDto);

        log.debug("Ответ для LMS сформирован: status={}", response.getStatus());
        return ResponseEntity.ok(response);
    }
}
