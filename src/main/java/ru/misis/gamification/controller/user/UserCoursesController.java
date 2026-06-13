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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.misis.gamification.dto.analytics.UserCourseGroupLeaderboardDto;
import ru.misis.gamification.dto.user.response.UserCoursesResponseDto;
import ru.misis.gamification.mapper.LeaderboardMapper;
import ru.misis.gamification.model.UserCourseGroupLeaderboardView;
import ru.misis.gamification.model.UserCoursesView;
import ru.misis.gamification.service.application.leaderboard.LeaderboardApplicationService;
import ru.misis.gamification.service.application.user.UserStatisticsApplicationService;

import java.util.List;

/**
 * REST-контроллер для получения списка всех курсов пользователя с глобальной статистикой.
 * <p>
 * Возвращает глобальный прогресс (уровень, очки, прогресс до следующего уровня)
 * + полный список курсов, на которые зачислен пользователь (с датами зачисления,
 * очками по курсу и группой).
 * </p>
 */
@PreAuthorize("hasRole('ADMIN') || hasRole('TEACHER') || hasRole('STUDENT')")
@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserCoursesController {

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Фасадный сервис управления статистикой пользователей
     */
    private final UserStatisticsApplicationService userStatisticsApplicationService;

    /**
     * Сервис аналитики и отчётов по геймификации
     */
    private final LeaderboardApplicationService leaderboardService;

    /**
     * Маппер лидербордов
     */
    private final LeaderboardMapper applicationModelMapper;

    @Operation(
            summary = "Список всех курсов пользователя + глобальный прогресс",
            description = """
                    Возвращает глобальную статистику пользователя (уровень, общие очки, 
                    прогресс до следующего уровня) и полный список курсов, на которые он зачислен.
                    Курсы отсортированы по дате зачисления (новые сверху).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешно получен список курсов",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserCoursesResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос (например, пустой userId)"),
            @ApiResponse(responseCode = "401", description = "Не авторизован. Отсутствует заголовок X-User-Id."),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён. Недостаточно прав."),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PreAuthorize("#userId == authentication.principal.userId")
    @GetMapping("/users/{userId}/courses")
    public ResponseEntity<UserCoursesResponseDto> getUserCourses(
            @PathVariable
            @NotBlank(message = "{user.id.required}")
            @Parameter(description = "Идентификатор пользователя из LMS", example = "student007")
            String userId) {

        log.debug("REST запрос списка курсов пользователя: userId={}", userId);

        UserCoursesView view = userStatisticsApplicationService.getUserCourses(userId);

        UserCoursesResponseDto dto = UserCoursesResponseDto.builder()
                .userId(view.getUserId())
                .totalPoints(view.getTotalPoints())
                .level(view.getLevel())
                .pointsToNextLevel(view.getPointsToNextLevel())
                .progressPercent(view.getProgressPercent())
                .courses(view.getCourses())
                .totalCourses(view.getCourses().size())
                .build();

        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "Персонализированный лидерборд по курсу (и опционально группе)",
            description = """
                     Возвращает пагинированный топ участников курса (все группы или конкретную группу) +
                     обязательные данные о текущем студенте: место, очки, уровень.
                     groupId — опциональный параметр.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешно получен лидерборд",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserCourseGroupLeaderboardDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса (page < 0, size > 100 и т.п.)"),
            @ApiResponse(responseCode = "401", description = "Не авторизован. Отсутствует заголовок X-User-Id."),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён. Недостаточно прав."),
            @ApiResponse(responseCode = "404", description = "Курс, группа или пользователь не найдены")
    })
    @PreAuthorize("#userId == authentication.principal.userId")
    @GetMapping("/course/{courseId}/user/{userId}")
    public ResponseEntity<UserCourseGroupLeaderboardDto> getLeaderboard(
            @PathVariable @NotBlank(message = "{course.id.required}")
            @Parameter(description = "Идентификатор курса из LMS", example = "MATH-101")
            String courseId,

            @PathVariable @NotBlank(message = "{user.id.required}")
            @Parameter(description = "Идентификатор пользователя из LMS", example = "student007")
            String userId,

            @RequestParam(required = false)
            @Parameter(description = "Идентификатор группы (опционально, если не указан — весь курс)", example = "M-21-2")
            String groupId,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "{page.non-negative}")
            @Parameter(description = "Номер страницы (0-based)", example = "0")
            int page,

            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE)
            @Min(value = 1, message = "{size.positive}")
            @Max(value = MAX_PAGE_SIZE, message = "{size.too-large}")
            @Parameter(description = "Размер страницы (макс " + MAX_PAGE_SIZE + ")", example = "50")
            int size) {

        log.debug("REST лидерборд: userId={}, courseId={}, groupId={}, page={}, size={}",
                userId, courseId, groupId, page, size);

        UserCourseGroupLeaderboardView view = leaderboardService.getCourseLeaderboardForUser(
                courseId, groupId, page, size, userId);
        UserCourseGroupLeaderboardDto lb = applicationModelMapper.toUserCourseGroupLeaderboardDto(view);

        return ResponseEntity.ok(lb);
    }
}