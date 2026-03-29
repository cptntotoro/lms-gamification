package ru.misis.gamification.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.gamification.dto.user.response.UserDto;
import ru.misis.gamification.dto.user.response.UserGlobalCourseGroupDto;
import ru.misis.gamification.mapper.UserMapper;
import ru.misis.gamification.model.UserProgressView;
import ru.misis.gamification.model.UserStatisticsView;
import ru.misis.gamification.service.application.user.UserProgressApplicationService;
import ru.misis.gamification.service.application.user.UserStatisticsApplicationService;

/**
 * Контроллер для получения данных пользователями (виджеты).
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Widget API", description = "API для виджетов и фронтенда пользователей")
public class UserController {

    /**
     * Фасадный сервис управления прогрессом очков и уровня пользователей
     */
    private final UserProgressApplicationService progressService;

    /**
     * Фасадный сервис управления статистикой пользователей
     */
    private final UserStatisticsApplicationService statisticsService;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    @GetMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.userId")
    @Operation(
            summary = "Данные пользователя для виджета",
            description = """
                     Возвращает глобальный прогресс пользователя.
                     Если передан courseId — добавляет статистику по курсу и (опционально) группе.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные успешно получены",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры (например, courseId без значения)"),
            @ApiResponse(responseCode = "401", description = "Не авторизован. Отсутствует заголовок X-User-Id."),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён. Недостаточно прав."),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<UserGlobalCourseGroupDto> getUserData(
            @PathVariable
            @NotBlank(message = "userId обязателен")
            @Parameter(description = "Внешний ID пользователя из LMS", example = "alex123", required = true)
            String userId,

            @RequestParam(required = false)
            @Parameter(description = "Идентификатор курса (если передан — возвращается статистика по курсу)", example = "CS-101-2025")
            String courseId,

            @RequestParam(required = false)
            @Parameter(description = "Идентификатор группы (опционально, только вместе с courseId)", example = "G-14")
            String groupId) {

        UserProgressView progress = progressService.getProgress(userId);
        UserGlobalCourseGroupDto userGlobalCourseGroupDto = userMapper.toUserGlobalCourseGroupDto(progress);

        if (courseId != null && !courseId.trim().isEmpty()) {
            UserStatisticsView stats = statisticsService.getUserStatistics(userId, courseId, groupId);

            userGlobalCourseGroupDto = userGlobalCourseGroupDto.toBuilder()
                    .courseId(stats.courseId())
                    .groupId(stats.groupId())
                    .pointsInCourse(stats.pointsInCourse())
                    .rankInCourse(stats.rankInCourse())
                    .rankInGroup(stats.rankInGroup())
                    .build();
        }

        return ResponseEntity.ok(userGlobalCourseGroupDto);
    }
}
