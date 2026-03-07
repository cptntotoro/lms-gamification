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
import ru.misis.gamification.mapper.LeaderboardMapper;
import ru.misis.gamification.model.UserCourseGroupLeaderboardView;
import ru.misis.gamification.service.application.leaderboard.LeaderboardApplicationService;

@Tag(name = "Web Pages", description = "Простые HTML-страницы приложения")
@Controller
@RequestMapping("/demo/leaderboard")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserLeaderboardPageController {

    /**
     * Сервис аналитики и отчётов по геймификации
     */
    private final LeaderboardApplicationService leaderboardService;

    /**
     * Маппер лидербордов
     */
    private final LeaderboardMapper leaderboardMapper;

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    @Operation(
            summary = "Персонализированный лидерборд по курсу (и опционально группе)",
            description = """
                     Отображает пагинированный топ участников курса (все группы или конкретную группу) +\s
                     место, очки и уровень текущего студента.
                     groupId — опциональный параметр.
                    \s"""
    )
    @GetMapping("/course/{courseId}/user/{userId}")
    public String getLeaderboard(
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
            int size,

            Model model
    ) {
        log.debug("Демо-лидерборд: userId={}, courseId={}, groupId={}, page={}, size={}",
                userId, courseId, groupId, page, size);

        UserCourseGroupLeaderboardView view = leaderboardService.getCourseLeaderboardForUser(
                courseId, groupId, page, size, userId);

        UserCourseGroupLeaderboardDto lb = leaderboardMapper.toUserCourseGroupLeaderboardDto(view);

        model.addAttribute("leaderboard", lb);
        model.addAttribute("courseId", courseId);
        model.addAttribute("groupId", groupId);

        return "leaderboard";
    }
}
