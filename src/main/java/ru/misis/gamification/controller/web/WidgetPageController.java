package ru.misis.gamification.controller.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.misis.gamification.dto.web.response.UserDto;
import ru.misis.gamification.service.progress.UserProgressService;

@Tag(name = "Web Pages", description = "Простые HTML-страницы приложения")
@Controller
@RequiredArgsConstructor
@Slf4j
public class WidgetPageController {

    /**
     * Сервис подготовки данных прогресса пользователя для виджета
     */
    private final UserProgressService userProgressService;

    @Operation(
            summary = "Главная страница",
            description = "Отображает демо-виджет геймификации с демонстрационными данными"
    )
    @GetMapping("/{userId}/widget")
    public String getUserWidget(@Parameter(description = "Внешний ID пользователя из LMS",
            required = true, example = "alex123") @PathVariable String userId, Model model) {
        log.debug("Открыт демо-виджет для пользователя: userId={}", userId);

        try {
            UserDto userDto = userProgressService.getProgress(userId);

            model.addAttribute("user", userDto);
            return "widget";
        } catch (Exception e) {
            log.warn("Ошибка при загрузке демо-виджета для userId={}: {}", userId, e.getMessage());
            model.addAttribute("error", "Данные пользователя недоступны");
            model.addAttribute("user", null);
            return "widget";
        }
    }
}
