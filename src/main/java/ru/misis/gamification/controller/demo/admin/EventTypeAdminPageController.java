package ru.misis.gamification.controller.demo.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.misis.gamification.dto.admin.response.EventTypeDto;
import ru.misis.gamification.mapper.EventTypeMapper;
import ru.misis.gamification.model.EventTypeSummary;
import ru.misis.gamification.service.application.eventtype.EventTypeAdminApplicationService;

@Tag(name = "Admin - Типы событий (страницы)", description = "HTML-страницы управления типами событий")
@Controller
@RequestMapping("/demo/admin/event-types")
@RequiredArgsConstructor
public class EventTypeAdminPageController {

    /**
     * Фасадный сервис управления типами событий для администратора
     */
    private final EventTypeAdminApplicationService eventTypeAdminService;

    /**
     * Маппер типов событий
     */
    private final EventTypeMapper eventTypeMapper;

    @Operation(summary = "Страница управления типами событий")
    @GetMapping
    public String getEventTypesPage(Pageable pageable, Model model) {
        Page<EventTypeSummary> eventTypeSummaries = eventTypeAdminService.findAll(pageable);
        Page<EventTypeDto> page = eventTypeSummaries.map(eventTypeMapper::toEventTypeDto);

        model.addAttribute("page", page);
        model.addAttribute("types", page.getContent());

        return "admin/event-types";
    }
}