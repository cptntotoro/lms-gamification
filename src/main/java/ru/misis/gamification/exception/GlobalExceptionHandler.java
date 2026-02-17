package ru.misis.gamification.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.misis.gamification.dto.lms.response.LmsEventResponsetDto;

/**
 * Глобальный обработчик исключений для всех REST-контроллеров.
 * Возвращает ответ в формате, понятном LMS-системе.
 */
@Slf4j
@RestControllerAdvice(basePackages = "ru.misis.gamification.controller")
public class GlobalExceptionHandler {

    // Самое частое исключение в вашем проекте
    @ExceptionHandler(DuplicateEventException.class)
    public ResponseEntity<LmsEventResponsetDto> handleDuplicateEvent(DuplicateEventException ex) {
        log.info("Обнаружен дубликат события: {}", ex.getMessage());
        return ResponseEntity.ok(LmsEventResponsetDto.duplicate(ex.getMessage().replaceAll(".*: ", "")));
    }

    // Пользователь не найден (хотя в текущей логике createIfNotExists не должен бросать)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<LmsEventResponsetDto> handleUserNotFound(UserNotFoundException ex) {
        log.warn("Ошибка: {}", ex.getMessage());
        return ResponseEntity.ok(LmsEventResponsetDto.error(ex.getMessage()));
    }

    // Нарушение уникальности в БД (race condition, дубликат и т.п.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<LmsEventResponsetDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Нарушение целостности данных: {}", ex.getMostSpecificCause().getMessage());
        // Чаще всего это дубликат event_id → возвращаем как duplicate
        return ResponseEntity.ok(LmsEventResponsetDto.duplicate("Событие уже обработано (обнаружено на уровне БД)"));
    }

    // Ошибки валидации @Valid (если добавите в будущем)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<LmsEventResponsetDto> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Ошибка валидации запроса");

        log.warn("Ошибка валидации входящего запроса: {}", message);
        return ResponseEntity.badRequest().body(LmsEventResponsetDto.error(message));
    }

    // Все остальные непредвиденные ошибки → 200 + error в теле (чтобы LMS не ломался)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<LmsEventResponsetDto> handleGenericException(Exception ex) {
        log.error("Необработанная ошибка при обработке запроса", ex);
        return ResponseEntity.internalServerError()
                .body(LmsEventResponsetDto.error("Внутренняя ошибка сервера геймификации"));
    }
}