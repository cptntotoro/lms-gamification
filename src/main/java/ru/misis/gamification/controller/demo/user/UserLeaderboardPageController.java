package ru.misis.gamification.controller.demo.user;

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
import ru.misis.gamification.dto.analytics.UserCourseGroupLeaderboardDto;
import ru.misis.gamification.mapper.ApplicationModelMapper;
import ru.misis.gamification.model.UserCourseGroupLeaderboardView;
import ru.misis.gamification.service.application.leaderboard.LeaderboardApplicationService;

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
    private final LeaderboardApplicationService leaderboardService;

    /**
     * Маппер моделей в DTO
     */
    private final ApplicationModelMapper applicationModelMapper;

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    @Operation(summary = "Лидерборд по курсу для текущего студента",
            description = "Возвращает топ-N студентов курса + место и очки текущего пользователя")
    @GetMapping("/course/{courseId}/groups/{groupId}/user/{userId}")
    public String getLeaderboardByCourseGroup(
            @PathVariable @NotBlank(message = "{course.id.required}")
            @Parameter(description = "Идентификатор курса из LMS") String courseId,

            @PathVariable
            @Parameter(description = "Идентификатор группы из LMS (null = весь курс)") String groupId,

            @PathVariable @NotBlank(message = "{user.id.required}")
            @Parameter(description = "Идентификатор пользователя из LMS", example = "student007") String userId,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "{page.non-negative}")
            @Parameter(description = "Номер страницы (0-based)") int page,

            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE)
            @Min(value = 1, message = "{size.positive}")
            @Max(value = MAX_PAGE_SIZE, message = "{size.too-large}")
            @Parameter(description = "Размер страницы (макс " + MAX_PAGE_SIZE + ")", example = "50") int size,

            Model model
    ) {
        UserCourseGroupLeaderboardView leaderboardForUser = leaderboardService.getCourseLeaderboardForUser(
                courseId, groupId, page, size, userId);

        UserCourseGroupLeaderboardDto lb = applicationModelMapper.toUserCourseGroupLeaderboardDto(leaderboardForUser);

        model.addAttribute("leaderboard", lb);
        model.addAttribute("courseId", courseId);
        model.addAttribute("groupId", groupId);

        return "leaderboard";
    }

    @GetMapping("/course/{courseId}/user/{userId}")
    public String getLeaderboardByCourse(
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
            @Parameter(description = "Размер страницы (макс " + MAX_PAGE_SIZE + ")", example = "50") int size,

            Model model) {

        UserCourseGroupLeaderboardView leaderboardForUser = leaderboardService.getCourseLeaderboardForUser(
                courseId, null, page, size, userId);

        UserCourseGroupLeaderboardDto lb = applicationModelMapper.toUserCourseGroupLeaderboardDto(leaderboardForUser);

        model.addAttribute("leaderboard", lb);
        model.addAttribute("courseId", courseId);
        model.addAttribute("groupId", null);
        return "leaderboard";
    }
}
