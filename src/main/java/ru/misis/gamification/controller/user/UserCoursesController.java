package ru.misis.gamification.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.gamification.dto.user.response.UserCoursesResponseDto;
import ru.misis.gamification.model.UserCoursesView;
import ru.misis.gamification.service.application.user.UserStatisticsApplicationService;

/**
 * REST-контроллер для получения списка всех курсов пользователя с глобальной статистикой.
 * <p>
 * Возвращает глобальный прогресс (уровень, очки, прогресс до следующего уровня)
 * + полный список курсов, на которые зачислен пользователь (с датами зачисления,
 * очками по курсу и группой).
 * </p>
 */
@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserCoursesController {

    /**
     * Фасадный сервис управления статистикой пользователей
     */
    private final UserStatisticsApplicationService userStatisticsApplicationService;

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
}