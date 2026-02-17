package ru.misis.gamification.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для HTML-страниц
 */
@Slf4j
@Controller
public class PageController {

    /**
     * Главная страница с демо-виджетом
     */
    @GetMapping("/")
    public String index() {
        log.debug("Открыта главная страница с демо-виджетом");
        return "index";
    }

    /**
     * Административная панель
     */
    @GetMapping("/admin")
    public String admin() {
        log.info("Открыта административная панель");
        return "admin";
    }
}
