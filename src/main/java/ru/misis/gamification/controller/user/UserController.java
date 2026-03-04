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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.gamification.dto.user.response.UserDto;
import ru.misis.gamification.dto.user.response.UserStatisticsDto;
import ru.misis.gamification.service.progress.UserProgressService;
import ru.misis.gamification.service.user.UserStatisticsService;

/**
 * Контроллер для получения данных пользователями (виджеты).
 */
@RestController
@RequestMapping("/api/v1/widget")
@RequiredArgsConstructor
@Tag(name = "Widget API", description = "API для виджетов и фронтенда пользователей")
public class UserController {

    /**
     * Сервис подготовки данных прогресса пользователя для виджета
     */
    private final UserProgressService userProgressService;

    private final UserStatisticsService userStatisticsService;

    /**
     * Получить прогресс пользователя для виджета
     */
    @GetMapping("/users/{userId}/progress")
    @Operation(
            summary = "Получить прогресс пользователя для виджета",
            description = "Возвращает текущий уровень, очки и базовую информацию для отображения в виджете"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Прогресс пользователя успешно получен",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<UserDto> getUserProgress(@Parameter(description = "Внешний ID пользователя из LMS",
            required = true, example = "alex123") @PathVariable String userId) {
        return ResponseEntity.ok(userProgressService.getProgress(userId));
    }

    @GetMapping("/users/{userId}")
    @Operation(
            summary = "Данные для виджета пользователя",
            description = """
                    Возвращает прогресс пользователя в контексте курса и (опционально) группы.
                    courseId — обязательный параметр.
                    groupId — опциональный (если передан, то возвращаются также данные по группе).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешно получены данные для виджета",
                    content = @Content(schema = @Schema(implementation = UserStatisticsDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные входные параметры"),
            @ApiResponse(responseCode = "404", description = "Пользователь / курс / группа / зачисление не найдены")
    })
    public ResponseEntity<UserStatisticsDto> getUserWidgetData(
            @PathVariable
            @NotBlank(message = "userId обязателен")
            @Parameter(description = "Внешний ID пользователя из LMS", example = "stud-98765", required = true)
            String userId,

            @RequestParam
            @NotBlank(message = "courseId обязателен")
            @Parameter(description = "Внешний идентификатор курса", example = "CS-101-2025", required = true)
            String courseId,

            @RequestParam(required = false)
            @Parameter(description = "Внешний идентификатор группы/потока", example = "G-14")
            String groupId) {

        return ResponseEntity.ok(userStatisticsService.getUserStatisticsGlobalAndByCourseAndGroup(userId, courseId, groupId));
    }
}
