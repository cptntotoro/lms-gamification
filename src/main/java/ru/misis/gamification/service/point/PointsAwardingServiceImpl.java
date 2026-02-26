package ru.misis.gamification.service.point;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.service.course.UserCourseService;
import ru.misis.gamification.service.event.EventTypeService;
import ru.misis.gamification.service.point.result.AwardResult;
import ru.misis.gamification.service.progress.LevelCalculatorService;
import ru.misis.gamification.service.transaction.TransactionService;
import ru.misis.gamification.service.user.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Сервис бизнес-логики начисления очков
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PointsAwardingServiceImpl implements PointsAwardingService {

    /**
     * Сервис управления транзакциями
     */
    private final TransactionService transactionService;

    /**
     * Сервис управления типами событий
     */
    private final EventTypeService eventTypeService;

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    /**
     * Сервис расчета уровня пользователя на основе накопленных очков
     */
    private final LevelCalculatorService levelCalculatorService;

    /**
     * Сервис управления курсами пользователей
     */
    private final UserCourseService userCourseService;

    @Transactional
    @Override
    public AwardResult awardPoints(LmsEventRequestDto request) {
        String userId = request.getUserId();
        String eventId = request.getEventId();
        String typeCode = request.getEventType();

        // 1. Проверка дубля
        if (transactionService.isExistsByEventId(eventId)) {
            log.info("Дубликат события: {}", eventId);
            return AwardResult.duplicate();
        }

        // 2. Получаем активный тип события
        EventType type;
        try {
            type = eventTypeService.getActiveByCode(typeCode);
        } catch (EventTypeNotFoundException e) {
            log.warn("Не найден активный тип события: {}", typeCode);
            return AwardResult.rejected("Неизвестный или отключённый тип события: " + typeCode);
        }

        // 3. Получаем или создаём пользователя
        User user = userService.createIfNotExists(userId, request.getCourseId(), request.getGroupId());

        // 4. Проверка дневного лимита
        long todaySum = transactionService.sumPointsByUserIdAndEventTypeAndDate(
                userId, typeCode, LocalDate.now());

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

        // 7. Начисление очков по курсу (если курс указан)
        if (request.getCourseId() != null) {
            userCourseService.addPointsToCourse(user, request.getCourseId(), points);
        }

        log.info("Начисление успешно: {} очков пользователю {}, новый уровень = {}, levelUp = {}",
                points, userId, user.getLevel(), levelUp);

        return AwardResult.success(
                points,
                newTotal,
                user.getLevel(),
                levelUp,
                savedTx.getUuid(),
                0L,
                0.0
        );
    }
}