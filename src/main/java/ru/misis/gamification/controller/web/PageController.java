package ru.misis.gamification.controller.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.misis.gamification.dto.web.response.UserDto;

/**
 * Контроллер для HTML-страниц
 */
@Controller
@Tag(name = "Web Pages", description = "Простые HTML-страницы приложения")
public class PageController {

    /**
     * Главная страница с демо-виджетом
     */
    @GetMapping("/")
    @Operation(summary = "Главная страница", description = "Отображает демо-виджет геймификации")
    public String index(Model model) {
        UserDto demo = UserDto.builder()
                .userId("AlexJohnson")
                .totalPoints(1250)
                .level(12)
                .build();

        model.addAttribute("user", demo);
        return "widget";
    }

    /**
     * Административная панель
     */
    @GetMapping("/admin")
    @Operation(summary = "Административная панель", description = "Отображает админ-панель (требуется авторизация)")
    public String admin() {
        return "admin";
    }
}
