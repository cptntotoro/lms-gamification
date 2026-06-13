package ru.misis.gamification.controller.demo.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.misis.gamification.dto.admin.response.EventTypeDto;
import ru.misis.gamification.mapper.EventTypeMapper;
import ru.misis.gamification.model.EventTypeSummary;
import ru.misis.gamification.service.application.eventtype.EventTypeAdminApplicationService;

@Controller
@RequestMapping("/demo/admin/event-types")
@RequiredArgsConstructor
public class EventTypeAdminPageController {

    private final EventTypeAdminApplicationService eventTypeAdminService;
    private final EventTypeMapper eventTypeMapper;

    /**
     * Страница управления типами событий
     */
    @GetMapping
    public String getEventTypesPage() {
        return "admin/event-types";
    }

    /**
     * Список всех типов событий с пагинацией
     */
    @GetMapping("/all-types")
    public ResponseEntity<Page<EventTypeDto>> getAll(Pageable pageable) {
        Page<EventTypeSummary> page = eventTypeAdminService.findAll(pageable);
        return ResponseEntity.ok(page.map(eventTypeMapper::toEventTypeDto));
    }
}