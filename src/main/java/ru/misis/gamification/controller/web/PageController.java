package ru.misis.gamification.controller.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
    public String index() {
        return "index";
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
