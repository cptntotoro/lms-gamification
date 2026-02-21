package ru.misis.gamification.controller.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.service.user.UserAdminService;

@Tag(name = "Admin - Пользователи (страницы)", description = "HTML-страницы админ-панели")
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UserAdminPageController {

    /**
     * Сервис управления пользователями для администратора
     */
    private final UserAdminService userAdminService;

    @GetMapping
    @Operation(summary = "Административная панель", description = "Отображает админ-панель (требуется авторизация)")
    public String admin() {
        return "admin";
    }

    @Operation(
            summary = "Страница профиля пользователя в админ-панели",
            description = "Отображает полную информацию о пользователе для администратора"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Страница успешно отображена"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/users/{userId}")
    public String getUserProfilePage(
            @Parameter(description = "Внешний ID пользователя из LMS", required = true, example = "alex123")
            @PathVariable String userId,
            Model model) {

        try {
            UserAdminDto user = userAdminService.findByUserId(userId);

            model.addAttribute("user", user);

            // Рассчитываем прогресс
            int nextLevelThreshold = 1000 + user.getLevel() * 500;
            double progressPercent = user.getTotalPoints() != null
                    ? Math.min((double) user.getTotalPoints() / nextLevelThreshold * 100, 100)
                    : 0;
            int pointsToNext = nextLevelThreshold - (user.getTotalPoints() != null ? user.getTotalPoints() : 0);

            model.addAttribute("nextLevelThreshold", nextLevelThreshold);
            model.addAttribute("progressPercent", progressPercent);
            model.addAttribute("pointsToNext", pointsToNext);

            return "user-profile";
        } catch (Exception e) {
            model.addAttribute("error", "Пользователь с ID '" + userId + "' не найден");
            model.addAttribute("user", null);
            return "user-profile";
        }
    }
}