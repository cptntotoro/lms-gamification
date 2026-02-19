package ru.misis.gamification.service;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.dto.lms.response.LmsEventResponsetDto;
import ru.misis.gamification.exception.DuplicateEventException;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.service.event.EventTypeService;
import ru.misis.gamification.service.transaction.TransactionService;
import ru.misis.gamification.service.user.UserService;

import java.time.LocalDate;

/**
 * Сервис обработки событий от LMS
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventManagementServiceImpl implements EventManagementService {

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    /**
     * Сервис управления транзакциями
     */
    private final TransactionService transactionService;

    /**
     * Сервис для работы с типами событий
     */
    private final EventTypeService eventTypeService;

    @Override
    @Transactional
    public LmsEventResponsetDto process(LmsEventRequestDto request) {
        if (request == null) {
            log.warn("Получен null запрос на обработку события");
            return LmsEventResponsetDto.error("Запрос события не может быть null");
        }

        String userId = request.getUserId();
        String eventId = request.getEventId();
        String eventTypeCode = request.getEventType();

        // Валидация
        if (StringUtils.isBlank(userId)) {
            return LmsEventResponsetDto.error("Идентификатор пользователя обязателен");
        }
        if (StringUtils.isBlank(eventId)) {
            return LmsEventResponsetDto.error("Идентификатор события обязателен");
        }
        if (StringUtils.isBlank(eventTypeCode)) {
            return LmsEventResponsetDto.error("Тип события (eventType) обязателен");
        }

        log.info("Начата обработка события: userId={}, eventId={}, type={}",
                userId, eventId, eventTypeCode);

        // 1. Получаем тип события
        EventType eventType;
        try {
            eventType = eventTypeService.getActiveByCode(eventTypeCode);
        } catch (EntityNotFoundException e) {
            log.warn("Не найден активный тип события: {}", eventTypeCode);
            return LmsEventResponsetDto.error("Неизвестный или отключённый тип события: " + eventTypeCode);
        }

        // 2. Проверяем дневной лимит по типу
        if (!eventTypeService.canAwardPoints(userId, eventTypeCode, eventType.getPoints(), LocalDate.now())) {
            log.warn("Превышен дневной лимит для типа {} у пользователя {}", eventTypeCode, userId);
            return LmsEventResponsetDto.error(
                    "Достигнут дневной лимит начисления по типу " + eventType.getDisplayName());
        }

        // 3. Создаём/получаем пользователя
        User user = userService.createIfNotExists(userId);

        // 4. Формируем транзакцию
        Transaction tx = Transaction.builder()
                .userId(userId)
                .eventId(eventId)
                .eventTypeCode(eventType.getTypeCode())
                .pointsEarned(eventType.getPoints())
                .description("Тип события: " + eventType.getDisplayName())
                .build();

        // 5. Сохраняем транзакцию (с защитой от дублей)
        Transaction savedTx;
        try {
            savedTx = transactionService.saveIfNotExists(tx);
        } catch (DuplicateEventException e) {
            log.info("Обнаружен дубликат события: eventId={}", eventId);
            return LmsEventResponsetDto.duplicate(eventId);
        } catch (DataIntegrityViolationException e) {
            log.info("Конфликт уникальности при сохранении события: eventId={}", eventId);
            return LmsEventResponsetDto.duplicate(eventId);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при сохранении транзакции: eventId={}", eventId, e);
            return LmsEventResponsetDto.error("Внутренняя ошибка сервера при обработке события");
        }

        // 6. Обновляем прогресс пользователя
        int oldPoints = user.getTotalPoints();
        user.setTotalPoints(oldPoints + eventType.getPoints());
        user.recalculateLevel();

        User updatedUser = userService.update(user);

        log.info("Успешно начислено {} очков (тип: {}) | userId={}, было {}, стало {}, уровень теперь {}",
                eventType.getPoints(), eventType.getTypeCode(),
                userId, oldPoints, updatedUser.getTotalPoints(), updatedUser.getLevel());

        // 7. Формируем успешный ответ
        return LmsEventResponsetDto.success(
                userId,
                eventType.getPoints(),
                updatedUser.getTotalPoints(),
                eventId,
                savedTx.getUuid(),
                eventType.getDisplayName()
        );
    }
}