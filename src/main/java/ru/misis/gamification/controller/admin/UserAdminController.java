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
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.mapper.TransactionMapper;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.service.transaction.TransactionService;
import ru.misis.gamification.service.user.UserAdminService;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - Пользователи", description = "Административные операции с пользователями и их транзакциями")
public class UserAdminController {

    /**
     * Сервис управления транзакциями
     */
    private final TransactionService transactionService;

    /**
     * Маппер транзакций
     */
    private final TransactionMapper transactionMapper;

    /**
     * Сервис управления пользователями
     */
    private final UserAdminService userAdminService;

    /**
     * Получение истории транзакций пользователя с пагинацией
     *
     * @param userId  Идентификатор пользователя из LMS
     * @param page    Номер страницы (0-based), по умолчанию 0
     * @param size    Размер страницы, по умолчанию 20
     * @param sortDir Направление сортировки: asc / desc (по умолчанию desc по дате)
     * @return Страница транзакций
     */
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
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.toUpperCase()), "createdAt");
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        Page<Transaction> transactionPage = transactionService.getTransactionsByUserId(userId, pageable);
        TransactionPageDto response = transactionMapper.toPageDto(transactionPage);

        return ResponseEntity.ok(response);
    }

    /**
     * Поиск пользователя по его ID из LMS (для админ-панели)
     *
     * @param userId внешний идентификатор пользователя из LMS
     * @return информация о пользователе
     * @throws UserNotFoundException если пользователь не найден
     */
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
        UserAdminDto dto = userAdminService.findByUserId(userId);
        return ResponseEntity.ok(dto);
    }
}