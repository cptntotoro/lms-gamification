package ru.misis.gamification.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.gamification.dto.analytics.UserCourseGroupLeaderboard;
import ru.misis.gamification.service.analytics.AnalyticsService;

@RestController
@RequestMapping("/api/me/leaderboard")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserLeaderboardController {

    /**
     * Сервис аналитики и отчётов по геймификации
     */
    private final AnalyticsService analyticsService;

    private static final int DEFAULT_SIZE = 50;
    private static final int MAX_SIZE = 100;

    /**
     * Лидерборд по курсу для текущего пользователя
     * Возвращает топ-N + обязательно место и очки текущего студента
     */
    @Operation(summary = "Лидерборд по курсу для текущего студента",
            description = "Возвращает топ-N студентов курса + место и очки текущего пользователя")
    @GetMapping("/course/{courseId}/groups/{groupId}")
    public ResponseEntity<UserCourseGroupLeaderboard> getCourseLeaderboard(
            @PathVariable @NotBlank(message = "{course.id.required}")
            @Parameter(description = "Код курса из LMS") String courseId,

            @RequestParam(name = "groupId")
            @Parameter(description = "Код группы из LMS (null = весь курс)") String groupId,

            @RequestParam(name = "userId")
            @NotBlank(message = "{user.id.required}")
            @Parameter(description = "ID текущего пользователя из LMS") String userId,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "{page.non-negative}")
            @Parameter(description = "Номер страницы (0-based)") int page,

            @RequestParam(defaultValue = "" + DEFAULT_SIZE)
            @Min(value = 1, message = "{size.positive}")
            @Max(value = MAX_SIZE, message = "{size.too-large}")
            @Parameter(description = "Размер страницы (макс " + MAX_SIZE + ")") int size
    ) {
        log.debug("Студенческий лидерборд курса: userId={}, courseId={}, page={}, size={}",
                userId, courseId, page, size);

        UserCourseGroupLeaderboard leaderboard = analyticsService.getCourseLeaderboardForUser(
                courseId,
                groupId,
                page,
                size,
                userId
        );

        return ResponseEntity.ok(leaderboard);
    }
}
