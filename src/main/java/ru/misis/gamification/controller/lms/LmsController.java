package ru.misis.gamification.controller.lms;

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
import ru.misis.gamification.service.EventManagementService;

/**
 * Контроллер для приема событий от LMS
 */
@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
@Slf4j
public class LmsController {

    /**
     * Сервис обработки событий от LMS
     */
    private final EventManagementService eventManagementService;

    @PostMapping
    public ResponseEntity<LmsEventResponsetDto> processUserEvent(@RequestBody @Valid LmsEventRequestDto lmsEventRequestDto) {
        log.info("Получен запрос от LMS: userId={}, eventId={}, eventType={}",
                lmsEventRequestDto.getUserId(), lmsEventRequestDto.getEventId(), lmsEventRequestDto.getEventType());

        LmsEventResponsetDto response = eventManagementService.process(lmsEventRequestDto);

        log.debug("Ответ для LMS сформирован: status={}", response.getStatus());
        return ResponseEntity.ok(response);
    }
}
