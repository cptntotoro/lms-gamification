package ru.misis.gamification.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.misis.gamification.dto.lms.response.LmsEventResponseDto;

/**
 * Глобальный обработчик исключений для всех REST-контроллеров
 */
@Slf4j
@RestControllerAdvice(basePackages = "ru.misis.gamification.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEventException.class)
    public ResponseEntity<LmsEventResponseDto> handleDuplicateEvent(DuplicateEventException ex) {
        log.info("Обнаружен дубликат события: {}", ex.getMessage());
        return ResponseEntity.ok(LmsEventResponseDto.duplicate(ex.getMessage().replaceAll(".*: ", "")));
    }

    @ExceptionHandler(DuplicateEventTypeException.class)
    public ResponseEntity<LmsEventResponseDto> handleDuplicateEventType(DuplicateEventTypeException ex) {
        log.info("Обнаружен дубликат типа события: {}", ex.getMessage());
        return ResponseEntity.ok(LmsEventResponseDto.duplicate(ex.getMessage().replaceAll(".*: ", "")));
    }

    @ExceptionHandler(UserNotEnrolledInCourseException.class)
    public ResponseEntity<LmsEventResponseDto> handleUserNotEnrolled(UserNotEnrolledInCourseException ex) {
        log.warn("Ошибка зачисления: {}", ex.getMessage());
        return ResponseEntity.ok(LmsEventResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler(UserCourseEnrollmentNotFoundException.class)
    public ResponseEntity<LmsEventResponseDto> handleCourseEnrollmentNotFound(UserCourseEnrollmentNotFoundException ex) {
        log.warn("Ошибка: {}", ex.getMessage());
        return ResponseEntity.ok(LmsEventResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<LmsEventResponseDto> handleCourseNotFound(CourseNotFoundException ex) {
        log.warn("Ошибка: {}", ex.getMessage());
        return ResponseEntity.ok(LmsEventResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<LmsEventResponseDto> handleGroupNotFound(GroupNotFoundException ex) {
        log.warn("Ошибка: {}", ex.getMessage());
        return ResponseEntity.ok(LmsEventResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<LmsEventResponseDto> handleUserNotFound(UserNotFoundException ex) {
        log.warn("Ошибка: {}", ex.getMessage());
        return ResponseEntity.ok(LmsEventResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler(EventTypeNotFoundException.class)
    public ResponseEntity<LmsEventResponseDto> handleTypeNotFound(EventTypeNotFoundException ex) {
        log.warn("Ошибка: {}", ex.getMessage());
        return ResponseEntity.ok(LmsEventResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<LmsEventResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Нарушение целостности данных: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.ok(LmsEventResponseDto.duplicate("Событие уже обработано (обнаружено на уровне БД)"));
    }

    // Ошибки валидации @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<LmsEventResponseDto> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Ошибка валидации запроса");

        log.warn("Ошибка валидации входящего запроса: {}", message);
        return ResponseEntity.badRequest().body(LmsEventResponseDto.error(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<LmsEventResponseDto> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst()
                .orElse("Ошибка валидации параметров");

        log.warn("Нарушение валидации параметров: {}", message);
        return ResponseEntity.badRequest().body(LmsEventResponseDto.error(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<LmsEventResponseDto> handleGenericException(Exception ex) {
        log.error("Необработанная ошибка при обработке запроса", ex);
        return ResponseEntity.internalServerError()
                .body(LmsEventResponseDto.error("Внутренняя ошибка сервера геймификации"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<LmsEventResponseDto> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Доступ запрещён: {}", ex.getMessage());
        return ResponseEntity.status(403)
                .body(LmsEventResponseDto.error("Доступ запрещён. У вас нет прав на эти данные."));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<LmsEventResponseDto> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Ошибка аутентификации: {}", ex.getMessage());
        return ResponseEntity.status(401)
                .body(LmsEventResponseDto.error("Не авторизован. Отсутствует заголовок X-User-Id."));
    }
}