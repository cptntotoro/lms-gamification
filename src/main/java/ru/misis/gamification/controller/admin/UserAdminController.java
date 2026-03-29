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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    @Operation(
            summary = "Получить пагинированный список пользователей",
            description = "Возвращает полную информацию о пользователях для административной панели с поддержкой фильтрации по курсу и группе"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен",
                    content = @Content(schema = @Schema(implementation = UserAdminDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён")
    })
    public ResponseEntity<Page<UserAdminDto>> getAllUsers(
            @RequestParam(required = false)
            @Parameter(description = "Идентификатор курса из LMS", example = "MATH-101")
            String courseId,

            @RequestParam(required = false)
            @Parameter(description = "Идентификатор группы (опционально). Если не указан — все пользователи курса",
                    example = "M-21-2")
            String groupId,

            @Parameter(description = "Номер страницы (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы (от 1 до 100)", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Направление сортировки (asc/desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir.toUpperCase());
        Sort sort = Sort.by(direction, "totalPoints");

        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(page, safeSize, sort);

        Page<UserAdminView> usersPage = adminApplicationService.findAll(courseId, groupId, pageable);

        Page<UserAdminDto> dtoPage = usersPage.map(userMapper::toUserAdminDto);

        return ResponseEntity.ok(dtoPage);
    }

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