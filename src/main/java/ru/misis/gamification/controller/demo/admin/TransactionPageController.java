package ru.misis.gamification.controller.demo.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Web Pages", description = "Простые HTML-страницы приложения")
@Controller
@RequestMapping("/demo/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionPageController {

    @Operation(summary = "Страница истории транзакций пользователя")
    @GetMapping
    public String transactionsPage() {
        return "admin/transactions";
    }
}