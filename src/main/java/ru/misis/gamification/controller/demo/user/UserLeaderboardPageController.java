package ru.misis.gamification.controller.demo.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.misis.gamification.dto.analytics.UserCourseGroupLeaderboardDto;
import ru.misis.gamification.dto.user.response.CourseWithEnrollmentDto;
import ru.misis.gamification.dto.user.response.GroupWithMembershipDto;
import ru.misis.gamification.mapper.LeaderboardMapper;
import ru.misis.gamification.model.UserCourseGroupLeaderboardView;
import ru.misis.gamification.service.application.leaderboard.LeaderboardApplicationService;
import ru.misis.gamification.service.application.user.UserStatisticsApplicationService;

import java.util.List;

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
     * Фасадный сервис управления статистикой пользователей
     */
    private final UserStatisticsApplicationService userStatisticsApplicationService;

    /**
     * Маппер лидербордов
     */
    private final LeaderboardMapper leaderboardMapper;

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Лидерборд
     */
    @GetMapping
    public String leaderboardPage() {
        return "leaderboard";
    }

    /**
     * Персонализированный лидерборд по курсу (и опционально группе)
     */
    @GetMapping("/course/{courseId}/user/{userId}")
    public String getLeaderboard(
            @PathVariable @NotBlank(message = "{course.id.required}")
            String courseId,
            @PathVariable @NotBlank(message = "{user.id.required}")
            String userId,
            @RequestParam(required = false)
            String groupId,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "{page.non-negative}")
            int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE)
            @Min(value = 1, message = "{size.positive}")
            @Max(value = MAX_PAGE_SIZE, message = "{size.too-large}")
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



    /**
     * Список всех курсов системы с признаком записи пользователя
     */
    @GetMapping("/courses/all")
    public ResponseEntity<List<CourseWithEnrollmentDto>> getAllCoursesWithEnrollmentStatus(
            @RequestParam @NotBlank String userId) {
        log.debug("REST запрос всех курсов с признаком записи для userId={}", userId);
        List<CourseWithEnrollmentDto> courses = userStatisticsApplicationService.getAllCoursesWithEnrollmentStatus(userId);
        return ResponseEntity.ok(courses);
    }

    /**
     * Список всех групп курса с признаком членства пользователя
     */
    @GetMapping("/courses/{courseId}/groups")
    public ResponseEntity<List<GroupWithMembershipDto>> getCourseGroupsWithMembership(
            @PathVariable @NotBlank String courseId,
            @RequestParam @NotBlank String userId) {
        log.debug("REST запрос групп курса {} с признаком членства для userId={}", courseId, userId);
        List<GroupWithMembershipDto> groups = userStatisticsApplicationService.getCourseGroupsWithMembership(courseId, userId);
        return ResponseEntity.ok(groups);
    }
}
