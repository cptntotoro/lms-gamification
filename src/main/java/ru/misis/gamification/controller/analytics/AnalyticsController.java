package ru.misis.gamification.controller.analytics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.gamification.dto.analytics.GroupLeaderboardPageDto;
import ru.misis.gamification.mapper.ApplicationModelMapper;
import ru.misis.gamification.model.LeaderboardPageView;
import ru.misis.gamification.service.application.leaderboard.LeaderboardApplicationService;

/**
 * REST-контроллер для получения лидербордов и аналитики геймификации
 * <p>
 * Предоставляет данные о ранжировании студентов по курсу и/или группе.
 * Все идентификаторы (курса, группы, пользователя) — внешние строки из LMS.
 * <p>
 */
@Tag(name = "Analytics — Лидерборд и статистика")
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final LeaderboardApplicationService leaderboardApplicationService;

    /**
     * Маппер моделей в DTO
     */
    private final ApplicationModelMapper applicationModelMapper;

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    @Operation(
            summary = "Лидерборд студентов группы внутри курса",
            description = "Возвращает пагинированный список студентов группы, отсортированный по очкам на курсе. "
                    + "Идентификаторы курса и группы — внешние строки из LMS."
    )
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Успешно получен лидерборд",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GroupLeaderboardPageDto.class)))
    )
    @GetMapping("/courses/{courseId}/groups/{groupId}/leaderboard")
    public ResponseEntity<GroupLeaderboardPageDto> getLeaderboardByCourseGroup(
            @PathVariable @NotBlank(message = "{course.id.required}")
            @Parameter(description = "Идентификатор курса из LMS", example = "MATH-101") String courseId,

            @PathVariable @NotBlank(message = "{group.id.required}")
            @Parameter(description = "Идентификатор группы из LMS", example = "M-21-2") String groupId,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "{page.non-negative}")
            @Parameter(description = "Номер страницы (0-based)", example = "0") int page,

            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE)
            @Min(value = 1, message = "{size.positive}")
            @Max(value = MAX_PAGE_SIZE, message = "{size.too-large}")
            @Parameter(description = "Размер страницы (макс " + MAX_PAGE_SIZE + ")", example = "50") int size
    ) {
        log.debug("Запрос лидерборда группы курса: courseId={}, groupId={}, page={}, size={}",
                courseId, groupId, page, size);

        LeaderboardPageView view = leaderboardApplicationService.getGroupLeaderboard(courseId, groupId, page, size);
        GroupLeaderboardPageDto dto = applicationModelMapper.toGroupLeaderboardPageDto(view);
        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "Лидерборд студентов внутри курса",
            description = "Возвращает пагинированный список студентов группы, отсортированный по очкам на курсе. "
                    + "Идентификаторы курса и группы — внешние строки из LMS."
    )
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Успешно получен лидерборд",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GroupLeaderboardPageDto.class)))
    )
    @GetMapping("/courses/{courseId}/leaderboard")
    public ResponseEntity<GroupLeaderboardPageDto> getLeaderboardByCourse(
            @PathVariable @NotBlank(message = "{course.id.required}")
            @Parameter(description = "Идентификатор курса из LMS", example = "MATH-101") String courseId,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "{page.non-negative}")
            @Parameter(description = "Номер страницы (0-based)", example = "0") int page,

            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE)
            @Min(value = 1, message = "{size.positive}")
            @Max(value = MAX_PAGE_SIZE, message = "{size.too-large}")
            @Parameter(description = "Размер страницы (макс " + MAX_PAGE_SIZE + ")", example = "50") int size
    ) {
        log.debug("Запрос общего лидерборда курса: courseId={}, page={}, size={}", courseId, page, size);

        LeaderboardPageView view = leaderboardApplicationService.getGroupLeaderboard(courseId, null, page, size);
        GroupLeaderboardPageDto dto = applicationModelMapper.toGroupLeaderboardPageDto(view);
        return ResponseEntity.ok(dto);
    }
}