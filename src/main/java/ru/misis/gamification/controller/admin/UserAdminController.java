package ru.misis.gamification.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.mapper.UserMapper;
import ru.misis.gamification.model.UserAdminView;
import ru.misis.gamification.service.application.user.UserAdminApplicationService;

@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - Пользователи", description = "Административные операции с пользователями и их транзакциями")
public class UserAdminController {

    /**
     * Фасадный сервис управления пользователями для администратора
     */
    private final UserAdminApplicationService adminApplicationService;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    @GetMapping("/{userId}")
    @Operation(
            summary = "Получить информацию о пользователе по ID из LMS",
            description = "Возвращает полную информацию о пользователе для административной панели"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserAdminDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный формат userId"),
            @ApiResponse(responseCode = "401", description = "Не авторизован. Отсутствует заголовок X-User-Id."),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён. Недостаточно прав."),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<UserAdminDto> getUserById(
            @Parameter(description = "Идентификатор пользователя из LMS", example = "user-12345")
            @PathVariable String userId) {

        UserAdminView dto = adminApplicationService.findByUserId(userId);
        return ResponseEntity.ok(userMapper.toUserAdminDto(dto));
    }
}