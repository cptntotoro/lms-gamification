package ru.misis.gamification.controller.analytics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.gamification.dto.analytics.GroupLeaderboardPageDto;
import ru.misis.gamification.service.analytics.AnalyticsService;

@Tag(name = "Analytics — Лидерборд и статистика")
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    /**
     * Сервис аналитики и отчётов по геймификации
     */
    private final AnalyticsService analyticsService;

    @Operation(
            summary = "Лидерборд студентов группы внутри курса",
            description = "Возвращает пагинированный список студентов группы, отсортированный по очкам на курсе. "
                    + "Идентификаторы курса и группы — внешние строки из LMS."
    )
    @ApiResponse(responseCode = "200", description = "Успешный ответ или бизнес-ошибка")
    @GetMapping("/courses/{courseId}/groups/{groupId}/leaderboard")
    public ResponseEntity<GroupLeaderboardPageDto> getGroupLeaderboard(
            @PathVariable @Parameter(description = "Внешний ID курса из LMS") String courseId,
            @PathVariable @Parameter(description = "Внешний ID группы из LMS") String groupId,
            @RequestParam(defaultValue = "0") @Parameter(description = "Номер страницы (0-based)") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Размер страницы") int size
    ) {
        GroupLeaderboardPageDto leaderboard = analyticsService.getGroupLeaderboard(
                courseId, groupId, page, size
        );
        return ResponseEntity.ok(leaderboard);
    }
}