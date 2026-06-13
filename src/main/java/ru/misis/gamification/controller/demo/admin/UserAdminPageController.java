package ru.misis.gamification.controller.demo.admin;

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
import ru.misis.gamification.mapper.UserMapper;
import ru.misis.gamification.model.UserAdminView;
import ru.misis.gamification.service.application.user.UserAdminApplicationService;

@Controller
@RequestMapping("/demo/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserAdminPageController {

    /**
     * Фасадный сервис управления пользователями для администратора
     */
    private final UserAdminApplicationService userProgressService;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    /**
     * Страница со списком пользователей, их курсов и групп
     */
    @GetMapping
    public String profilesPage() {
        return "admin/profiles";
    }

    /**
     * Страница профиля пользователя в админ-панели
     */
    @GetMapping("/{userId}")
    public String getUserProfilePage(@PathVariable String userId, Model model) {

        log.debug("Админ открыл профиль пользователя: userId={}", userId);

        try {
            UserAdminView userAdminView = userProgressService.findByUserId(userId);
            UserAdminDto userAdminDto = userMapper.toUserAdminDto(userAdminView);
            model.addAttribute("user", userAdminDto);
            model.addAttribute("nextLevel", userAdminDto.getLevel() + 1);
            return "admin/user-profile";
        } catch (UserNotFoundException e) {
            log.warn("Пользователь не найден: userId={}", userId, e);
            model.addAttribute("error", "Пользователь с ID '" + userId + "' не найден");
            model.addAttribute("user", null);
            return "admin/user-profile";
        }
    }
}