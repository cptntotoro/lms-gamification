package ru.misis.gamification.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.gamification.dto.web.response.UserDto;
import ru.misis.gamification.mapper.UserMapper;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.service.user.UserService;

/**
 * Контроллер для получения данных пользователями (виджеты).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class WidgetController {

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    private final UserMapper userMapper;

    /**
     * Получить прогресс пользователя для виджета
     */
    @GetMapping("/{userId}/progress")
    public ResponseEntity<UserDto> getUserProgress(@PathVariable String userId) {
        User user = userService.get(userId);
        UserDto userDto = userMapper.userToUserDto(user);
        return ResponseEntity.ok(userDto);

    }
}
