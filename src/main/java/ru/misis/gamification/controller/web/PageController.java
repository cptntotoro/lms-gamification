package ru.misis.gamification.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для HTML-страниц
 */
@Controller
public class PageController {

    /**
     * Главная страница с демо-виджетом
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Административная панель
     */
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
