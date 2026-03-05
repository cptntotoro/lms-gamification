package ru.misis.gamification.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.gamification.dto.admin.response.TransactionPageDto;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.mapper.ApplicationModelMapper;
import ru.misis.gamification.model.TransactionSummary;
import ru.misis.gamification.model.UserAdminView;
import ru.misis.gamification.service.application.transaction.TransactionHistoryApplicationService;
import ru.misis.gamification.service.application.user.UserAdminApplicationService;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - Пользователи", description = "Административные операции с пользователями и их транзакциями")
public class UserAdminController {

    private final TransactionHistoryApplicationService transactionHistoryApplicationService;

    /**
     * Сервис управления пользователями
     */
    private final UserAdminApplicationService adminApplicationService;

    /**
     * Маппер моделей в DTO
     */
    private final ApplicationModelMapper applicationModelMapper;

    @Operation(
            summary = "Получить историю транзакций пользователя",
            description = "Возвращает пагинированный список транзакций для указанного пользователя с сортировкой по дате"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список транзакций успешно получен",
                    content = @Content(schema = @Schema(implementation = TransactionPageDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры пагинации или сортировки"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{userId}/transactions")
    public ResponseEntity<TransactionPageDto> getUserTransactions(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir.toUpperCase());
        Sort sort = Sort.by(direction, "createdAt");

        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(page, safeSize, sort);

        Page<TransactionSummary> transactionPage =
                transactionHistoryApplicationService.getTransactionsByUserId(userId, pageable);

        TransactionPageDto response =
                applicationModelMapper.toTransactionPageDto(transactionPage);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Получить информацию о пользователе по ID из LMS",
            description = "Возвращает полную информацию о пользователе для административной панели"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserAdminDto.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректный формат userId")
    })
    public ResponseEntity<UserAdminDto> getUserById(@PathVariable String userId) {
        UserAdminView dto = adminApplicationService.findByUserId(userId);
        return ResponseEntity.ok(applicationModelMapper.toUserAdminDto(dto));
    }
}