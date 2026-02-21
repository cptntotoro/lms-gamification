package ru.misis.gamification.controller.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.misis.gamification.dto.web.response.UserDto;
import ru.misis.gamification.mapper.UserMapper;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.service.user.UserService;

@Tag(name = "Web Pages", description = "Простые HTML-страницы приложения")
@Controller
@RequiredArgsConstructor
public class WidgetPageController {

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    @Operation(
            summary = "Главная страница",
            description = "Отображает демо-виджет геймификации с демонстрационными данными"
    )
    @GetMapping("/{userId}/widget")
    public String getUserWidget(@Parameter(description = "Внешний ID пользователя из LMS",
            required = true, example = "alex123") @PathVariable String userId, Model model) {
        User user = userService.get(userId);
        UserDto userDto = userMapper.userToUserDto(user);
        model.addAttribute("user", userDto);
        return "widget";
    }
}
