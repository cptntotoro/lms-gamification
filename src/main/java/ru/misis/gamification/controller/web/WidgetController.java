package ru.misis.gamification.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.gamification.dto.admin.response.TransactionResponseDto;
import ru.misis.gamification.dto.web.response.WidgetPointsDto;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.service.UserService;

import java.util.List;

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

    /**
     * Получить прогресс пользователя для виджета
     */
    @GetMapping("/{userId}/progress")
    public ResponseEntity<WidgetPointsDto> getUserProgress(@PathVariable String userId) {
        try {
            WidgetPointsDto response = userService.get(userId);
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Получить историю транзакций пользователя
     */
    @GetMapping("/{userId}/transactions")
    public ResponseEntity<List<TransactionResponseDto>> getUserTransactions(
            @PathVariable String userId) {
        try {
            List<TransactionResponseDto> transactions = userService.getUserTransactions(userId);
            return ResponseEntity.ok(transactions);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Получить последние N транзакций пользователя
     */
    @GetMapping("/{userId}/transactions/latest")
    public ResponseEntity<List<TransactionResponseDto>> getLatestTransactions(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<TransactionResponseDto> transactions = userService.getLatestTransactions(userId, limit);
            return ResponseEntity.ok(transactions);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
