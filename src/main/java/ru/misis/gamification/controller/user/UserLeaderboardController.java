package ru.misis.gamification.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.gamification.dto.analytics.UserCourseGroupLeaderboardDto;
import ru.misis.gamification.mapper.ApplicationModelMapper;
import ru.misis.gamification.model.UserCourseGroupLeaderboardView;
import ru.misis.gamification.service.application.leaderboard.LeaderboardApplicationService;

/**
 * REST-контроллер для получения персонализированного лидерборда студента.
 * <p>
 * Предоставляет данные о топ-N участников курса (или группы) + обязательную информацию
 * о месте и очках текущего пользователя.
 * <p>
 */
@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserLeaderboardController {

    /**
     * Сервис аналитики и отчётов по геймификации
     */
    private final LeaderboardApplicationService leaderboardService;

    /**
     * Маппер моделей в DTO
     */
    private final ApplicationModelMapper applicationModelMapper;

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    @Operation(summary = "Лидерборд по курсу и группе для текущего студента",
            description = """
                    Возвращает пагинированный топ участников курса (или группы) +\s
                    обязательные данные о текущем студенте: место, очки, уровень
                    """)
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Успешно получен лидерборд",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserCourseGroupLeaderboardDto.class)))
    )
    @GetMapping("/course/{courseId}/groups/{groupId}/user/{userId}")
    public ResponseEntity<UserCourseGroupLeaderboardDto> getCourseLeaderboard(
            @PathVariable @NotBlank(message = "{course.id.required}")
            @Parameter(description = "Идентификатор курса из LMS", example = "MATH-101") String courseId,

            @PathVariable @NotBlank(message = "{group.id.required}")
            @Parameter(description = "Идентификатор группы из LMS", example = "M-21-2") String groupId,

            @PathVariable @NotBlank(message = "{user.id.required}")
            @Parameter(description = "Идентификатор пользователя из LMS", example = "student007") String userId,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "{page.non-negative}")
            @Parameter(description = "Номер страницы (0-based)", example = "0") int page,

            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE)
            @Min(value = 1, message = "{size.positive}")
            @Max(value = MAX_PAGE_SIZE, message = "{size.too-large}")
            @Parameter(description = "Размер страницы (макс " + MAX_PAGE_SIZE + ")", example = "50") int size
    ) {
        log.debug("Студенческий лидерборд курса: userId={}, courseId={}, page={}, size={}",
                userId, courseId, page, size);

        UserCourseGroupLeaderboardView leaderboardForUser = leaderboardService.getCourseLeaderboardForUser(
                courseId, groupId, page, size, userId);
        UserCourseGroupLeaderboardDto lb = applicationModelMapper.toUserCourseGroupLeaderboardDto(leaderboardForUser);

        return ResponseEntity.ok(lb);
    }

    @Operation(
            summary = "Лидерборд курса (все группы) для студента",
            description = "Возвращает топ-N участников курса (все группы) + место и очки текущего студента"
    )
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Успешно получен лидерборд",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserCourseGroupLeaderboardDto.class)))
    )
    @GetMapping("/course/{courseId}/user/{userId}")
    public ResponseEntity<UserCourseGroupLeaderboardDto> getLeaderboardByCourse(
            @PathVariable @NotBlank(message = "{course.id.required}")
            @Parameter(description = "Идентификатор курса из LMS", example = "MATH-101") String courseId,

            @PathVariable @NotBlank(message = "{user.id.required}")
            @Parameter(description = "Идентификатор пользователя из LMS", example = "student007") String userId,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "{page.non-negative}")
            @Parameter(description = "Номер страницы (0-based)", example = "0") int page,

            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE)
            @Min(value = 1, message = "{size.positive}")
            @Max(value = MAX_PAGE_SIZE, message = "{size.too-large}")
            @Parameter(description = "Размер страницы (макс " + MAX_PAGE_SIZE + ")", example = "50") int size
    ) {
        log.debug("Запрос лидерборда (по курсу): userId={}, courseId={}, page={}, size={}",
                userId, courseId, page, size);

        UserCourseGroupLeaderboardView leaderboardForUser = leaderboardService.getCourseLeaderboardForUser(
                courseId, null, page, size, userId);
        UserCourseGroupLeaderboardDto lb = applicationModelMapper.toUserCourseGroupLeaderboardDto(leaderboardForUser);

        return ResponseEntity.ok(lb);
    }
}
