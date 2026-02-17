package ru.misis.gamification.service;

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
        String eventId = lmsEvent.getEventId();
        String userId = lmsEvent.getUserId();
        Integer pointsEarned = lmsEvent.getPointsEarned();

        if (pointsEarned == null || pointsEarned <= 0) {
            log.warn("Получено событие с некорректным количеством очков: userId={}, eventId={}, points={}",
                    userId, eventId, pointsEarned);
            return LmsEventResponsetDto.error("Некорректное количество начисляемых очков");
        }

        log.info("Обработка события от LMS → userId={}, eventId={}, points={}", userId, eventId, pointsEarned);

        User user = userService.createIfNotExists(userId);
        log.debug("Пользователь получен/создан: id={}, totalPoints до={}", user.getId(), user.getTotalPoints());

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .eventId(eventId)
                .pointsEarned(pointsEarned)
                .build();

        Transaction savedTransaction;
        try {
            savedTransaction = transactionService.saveIfNotExists(transaction);
            log.info("Транзакция успешно сохранена: transactionId={}, eventId={}",
                    savedTransaction.getId(), eventId);

        } catch (DuplicateEventException | DataIntegrityViolationException e) {
            log.info("Событие уже было обработано ранее (дубликат): eventId={}", eventId);
            return LmsEventResponsetDto.duplicate(eventId);
        } catch (Exception e) {
            log.error("Ошибка при сохранении транзакции: userId={}, eventId={}, points={}",
                    userId, eventId, pointsEarned, e);
            return LmsEventResponsetDto.error("Внутренняя ошибка при сохранении транзакции");
        }

        // Только после успешного сохранения транзакции обновляем пользователя
        int oldPoints = user.getTotalPoints();
        user.setTotalPoints(oldPoints + pointsEarned);
        user.recalculateLevel();

        User updatedUser = userService.update(user);

        log.info("Очки начислены: userId={}, было {}, стало {}, уровень теперь {}",
                userId, oldPoints, updatedUser.getTotalPoints(), updatedUser.getLevel());

        return LmsEventResponsetDto.success(
                userId,
                pointsEarned,
                updatedUser.getTotalPoints(),
                eventId,
                savedTransaction.getId()
        );
    }
}
