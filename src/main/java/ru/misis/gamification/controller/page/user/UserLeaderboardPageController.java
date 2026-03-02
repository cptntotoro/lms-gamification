package ru.misis.gamification.controller.page.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.misis.gamification.dto.analytics.UserCourseGroupLeaderboard;
import ru.misis.gamification.service.analytics.AnalyticsService;

@Tag(name = "Web Pages", description = "Простые HTML-страницы приложения")
@Controller
@RequestMapping("/api/demo/me/leaderboard")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserLeaderboardPageController {

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
    public String getLeaderboardByGroup(
            @PathVariable @NotBlank(message = "{course.id.required}")
            @Parameter(description = "Код курса из LMS") String courseId,

            @PathVariable
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
            @Parameter(description = "Размер страницы (макс " + MAX_SIZE + ")") int size,

            Model model
    ) {
        UserCourseGroupLeaderboard lb = analyticsService.getCourseLeaderboardForUser(
                courseId, groupId, page, size, userId);

        model.addAttribute("leaderboard", lb);
        model.addAttribute("courseId", courseId);
        model.addAttribute("groupId", groupId);

        return "leaderboard";
    }

    @GetMapping("/course/{courseId}")
    public String getLeaderboardByCourse(
            @PathVariable @NotBlank String courseId,
            @RequestParam @NotBlank String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size,
            Model model) {

        UserCourseGroupLeaderboard lb = analyticsService.getCourseLeaderboardForUser(
                courseId, null, page, size, userId);   // ← передаём null вместо groupId

        model.addAttribute("leaderboard", lb);
        model.addAttribute("courseId", courseId);
        model.addAttribute("groupId", null);
        return "leaderboard";
    }
}
