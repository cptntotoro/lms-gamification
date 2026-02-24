package ru.misis.gamification.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.gamification.dto.web.response.UserDto;
import ru.misis.gamification.service.progress.UserProgressService;

/**
 * Контроллер для получения данных пользователями (виджеты).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Widget API", description = "API для виджетов и фронтенда пользователей")
public class WidgetController {

    /**
     * Сервис подготовки данных прогресса пользователя для виджета
     */
    private final UserProgressService userProgressService;

    /**
     * Получить прогресс пользователя для виджета
     */
    @GetMapping("/{userId}/progress")
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
}
