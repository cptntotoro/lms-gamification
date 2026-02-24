package ru.misis.gamification.controller.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.service.progress.UserProgressService;

@Tag(name = "Admin - Пользователи (страницы)", description = "HTML-страницы админ-панели")
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class UserAdminPageController {

    /**
     * Сервис подготовки данных прогресса пользователя для виджета и админ-панели
     */
    private final UserProgressService userProgressService;

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

        log.debug("Админ открыл профиль пользователя: userId={}", userId);

        try {
            UserAdminDto user = userProgressService.getAdminProgress(userId);
            model.addAttribute("user", user);
            model.addAttribute("nextLevel", user.getLevel() + 1);
            return "admin/user-profile";
        } catch (UserNotFoundException e) {
            log.warn("Пользователь не найден: userId={}", userId, e);
            model.addAttribute("error", "Пользователь с ID '" + userId + "' не найден");
            model.addAttribute("user", null);
            return "admin/user-profile";
        }
//        catch (Exception e) {
//            log.error("Ошибка при загрузке профиля userId={}", userId, e);
//            model.addAttribute("error", "Произошла ошибка при загрузке профиля");
//            model.addAttribute("user", null);
//            return "admin/user-profile";
//        }
    }
}