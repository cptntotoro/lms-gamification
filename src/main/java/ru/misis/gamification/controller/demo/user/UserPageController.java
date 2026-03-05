package ru.misis.gamification.controller.demo.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.misis.gamification.dto.user.response.UserDto;
import ru.misis.gamification.mapper.ApplicationModelMapper;
import ru.misis.gamification.model.UserProgressView;
import ru.misis.gamification.service.application.user.UserProgressApplicationService;

@Tag(name = "Web Pages", description = "Простые HTML-страницы приложения")
@Controller
@RequiredArgsConstructor
@Slf4j
public class UserPageController {

    /**
     * Сервис подготовки данных прогресса пользователя для виджета
     */
    private final UserProgressApplicationService userProgressService;

    /**
     * Маппер моделей в DTO
     */
    private final ApplicationModelMapper applicationModelMapper;

    @Operation(
            summary = "Главная страница",
            description = "Отображает демо-виджет геймификации с демонстрационными данными"
    )
    @GetMapping("/{userId}/widget")
    public String getUserWidget(@Parameter(description = "Идентификатор пользователя из LMS",
            required = true, example = "alex123") @PathVariable String userId, Model model) {
        log.debug("Открыт демо-виджет для пользователя: userId={}", userId);

        try {
            UserProgressView userProgressView = userProgressService.getProgress(userId);
            UserDto userDto = applicationModelMapper.toUserDto(userProgressView);

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
