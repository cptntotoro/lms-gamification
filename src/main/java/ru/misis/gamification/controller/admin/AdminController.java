package ru.misis.gamification.controller.admin;

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
import ru.misis.gamification.dto.admin.TransactionPageDto;
import ru.misis.gamification.mapper.TransactionMapper;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.service.TransactionService;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    /**
     * Сервис управления транзакциями
     */
    private final TransactionService transactionService;

    /**
     * Маппер транзакций
     */
    private final TransactionMapper transactionMapper;

    /**
     * Получение истории транзакций пользователя с пагинацией
     *
     * @param userId  Идентификатор пользователя из LMS
     * @param page    Номер страницы (0-based), по умолчанию 0
     * @param size    Размер страницы, по умолчанию 20
     * @param sortDir Направление сортировки: asc / desc (по умолчанию desc по дате)
     * @return Страница транзакций
     */
    @GetMapping("/users/{userId}/transactions")
    public ResponseEntity<TransactionPageDto> getUserTransactions(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.toUpperCase()), "createdAt");
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort); // защита от огромных страниц

        Page<Transaction> transactionPage = transactionService.getTransactionsByUserId(userId, pageable);

        TransactionPageDto response = transactionMapper.toPageDto(transactionPage);

        return ResponseEntity.ok(response);
    }
}