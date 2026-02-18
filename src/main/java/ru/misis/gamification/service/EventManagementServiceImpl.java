package ru.misis.gamification.service;

import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.misis.gamification.dto.lms.response.LmsEventResponsetDto;
import ru.misis.gamification.exception.DuplicateEventException;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.model.entity.LmsEvent;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.service.transaction.TransactionService;
import ru.misis.gamification.service.user.UserService;

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

    @Override
    @Transactional
    public LmsEventResponsetDto process(LmsEvent lmsEvent) {
        if (lmsEvent == null) {
            log.warn("Получен null LmsEvent");
            return LmsEventResponsetDto.error("Событие не может быть null");
        }

        String userId = lmsEvent.getUserId();
        String eventId = lmsEvent.getEventId();
        Integer points = lmsEvent.getPointsEarned();

        if (StringUtils.isBlank(userId)) {
            return LmsEventResponsetDto.error("Идентификатор пользователя обязателен");
        }
        if (StringUtils.isBlank(eventId)) {
            return LmsEventResponsetDto.error("Идентификатор события обязателен");
        }
        if (points == null || points <= 0) {
            log.warn("Некорректное количество очков: userId={}, eventId={}, points={}", userId, eventId, points);
            return LmsEventResponsetDto.error("Количество начисляемых очков должно быть положительным числом");
        }

        log.info("Начата обработка события: userId={}, eventId={}, points={}", userId, eventId, points);

        User user = userService.createIfNotExists(userId);

        Transaction tx = Transaction.builder()
                .userId(userId)
                .eventId(eventId)
                .pointsEarned(points)
                .build();

        Transaction savedTx;
        try {
            savedTx = transactionService.saveIfNotExists(tx);
        } catch (DuplicateEventException | DataIntegrityViolationException e) {
            log.info("Дубликат события: eventId={}", eventId);
            return LmsEventResponsetDto.duplicate(eventId);
        } catch (Exception e) {
            log.error("Ошибка сохранения транзакции", e);
            return LmsEventResponsetDto.error("Внутренняя ошибка при сохранении");
        }

        int oldPoints = user.getTotalPoints();
        user.setTotalPoints(oldPoints + points);
        user.recalculateLevel();

        User updatedUser = userService.update(user);

        log.info("Очки начислены: userId={}, было {}, стало {}, уровень теперь {}",
                userId, oldPoints, updatedUser.getTotalPoints(), updatedUser.getLevel());

        return LmsEventResponsetDto.success(
                userId,
                points,
                updatedUser.getTotalPoints(),
                eventId,
                savedTx.getUuid()
        );
    }
}