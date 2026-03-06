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
import ru.misis.gamification.mapper.TransactionMapper;
import ru.misis.gamification.model.TransactionSummary;
import ru.misis.gamification.service.application.transaction.TransactionHistoryApplicationService;

@Slf4j
@RestController
@RequestMapping("/api/admin/transactions/")
@RequiredArgsConstructor
@Tag(name = "Admin - Пользователи", description = "Административные операции с транзакциями")
public class TransactionAdminController {

    /**
     * Фасадный сервис управления историей транзакций
     */
    private final TransactionHistoryApplicationService transactionHistoryApplicationService;

    /**
     * Маппер транзакций
     */
    private final TransactionMapper transactionMapper;

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
                transactionMapper.toTransactionPageDto(transactionPage);

        return ResponseEntity.ok(response);
    }
}
