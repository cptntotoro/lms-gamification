package ru.misis.gamification.service.point;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.dto.result.AwardResult;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.service.event.EventTypeService;
import ru.misis.gamification.service.level.LevelCalculatorService;
import ru.misis.gamification.service.transaction.TransactionService;
import ru.misis.gamification.service.user.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PointsServiceImpl implements PointsService {

    /**
     * Сервис управления транзакциями
     */
    private final TransactionService transactionService;

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    /**
     * Сервис расчета уровней
     */
    private final LevelCalculatorService levelCalculatorService;

    /**
     * Сервис управления типами событий
     */
    private final EventTypeService eventTypeService;

    @Override
    @Transactional
    public AwardResult awardPoints(LmsEventRequestDto request) {
        String userId = request.getUserId();
        String eventId = request.getEventId();
        String typeCode = request.getEventType();

        log.debug("Начало начисления очков: userId={}, eventId={}, typeCode={}", userId, eventId, typeCode);

        // 1. Проверка дубля
        if (transactionService.isExistsByEventId(eventId)) {
            log.info("Дубликат события: {}", eventId);
            return AwardResult.duplicate();
        }

        // 2. Тип события
        EventType type;
        try {
            type = eventTypeService.getActiveByCode(typeCode);
        } catch (EventTypeNotFoundException e) {
            log.warn("Не найден активный тип события: {}", typeCode);
            return AwardResult.rejected("Неизвестный или отключённый тип события: " + typeCode);
        }

        // 3. Получаем или создаём пользователя (с блокировкой внутри сервиса)
        User user = userService.createIfNotExists(userId);

        // 4. Проверка дневного лимита
        LocalDate today = LocalDate.now();
        long todaySum = transactionService.sumPointsByUserIdAndEventTypeAndDate(
                userId, typeCode, today);

        int points = type.getPoints();

        if (type.getMaxDailyPoints() != null && todaySum + points > type.getMaxDailyPoints()) {
            String reason = "Превышен дневной лимит по типу " + type.getDisplayName();
            log.warn(reason);
            return AwardResult.rejected(reason);
        }

        // 5. Создание транзакции
        Transaction tx = Transaction.builder()
                .userId(userId)
                .eventId(eventId)
                .eventTypeCode(typeCode)
                .pointsEarned(points)
                .description("Начисление за " + type.getDisplayName())
                .createdAt(LocalDateTime.now())
                .build();

        Transaction savedTx = transactionService.saveIfNotExists(tx);

        // 6. Обновление пользователя
        int oldLevel = user.getLevel();
        int newTotal = user.getTotalPoints() + points;
        user.setTotalPoints(newTotal);
        user.setLevel(levelCalculatorService.calculateLevel(newTotal));
        userService.update(user);

        boolean levelUp = user.getLevel() > oldLevel;

        log.info("Успешно начислено {} очков → userId={}, total={}, level={}, levelUp={}",
                points, userId, newTotal, user.getLevel(), levelUp);

        return AwardResult.success(
                points,
                newTotal,
                user.getLevel(),
                levelUp,
                savedTx.getUuid(),
                levelCalculatorService.pointsToNextLevel(user.getLevel())
        );
    }
}
