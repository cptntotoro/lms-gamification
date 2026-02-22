package ru.misis.gamification.service.point;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.dto.AwardResult;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.exception.InvalidEventTypeException;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.repository.EventTypeRepository;
import ru.misis.gamification.repository.TransactionRepository;
import ru.misis.gamification.repository.UserRepository;
import ru.misis.gamification.service.level.LevelCalculator;
import ru.misis.gamification.service.user.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PointsServiceImpl implements PointsService {

    private final TransactionRepository transactionRepo;
    private final UserRepository userRepo;
    private final EventTypeRepository eventTypeRepo;
    private final LevelCalculator levelCalculator;
    private final UserService userService;

    @Override
    @Transactional
    public AwardResult awardPoints(LmsEventRequestDto request) {
        String userId = request.getUserId();
        String eventId = request.getEventId();
        String typeCode = request.getEventType();

        // 1. Проверка дубля
        if (transactionRepo.existsByEventId(eventId)) {
            return AwardResult.duplicate();
        }

        // 2. Тип события
        EventType type = eventTypeRepo.findByTypeCodeAndActiveTrue(typeCode)
                .orElseThrow(() -> new EventTypeNotFoundException(typeCode));

        // 3. Пользователь с блокировкой
        User user = userRepo.findByUserIdForUpdate(userId)
                .orElseGet(() -> userService.createIfNotExists(userId));

        // 4. Дневной лимит
        LocalDate today = LocalDate.now();
        long todaySum = transactionRepo.sumPointsByUserIdAndEventTypeAndDate(
                userId, typeCode, today);

        int points = type.getPoints();

        if (type.getMaxDailyPoints() != null && todaySum + points > type.getMaxDailyPoints()) {
            return AwardResult.rejected("Превышен дневной лимит по типу " + type.getDisplayName());
        }

        // 5. Транзакция
        Transaction tx = Transaction.builder()
                .userId(userId)
                .eventId(eventId)
                .eventTypeCode(typeCode)
                .pointsEarned(points)
                .description("Начисление за " + type.getDisplayName())
                .createdAt(LocalDateTime.now())
                .build();

        Transaction savedTx = transactionRepo.save(tx);

        // 6. Обновление пользователя
        int oldLevel = user.getLevel();
        int newTotal = user.getTotalPoints() + points;
        user.setTotalPoints(newTotal);
        user.recalculateLevel(); // твой метод
        userRepo.save(user);

        boolean levelUp = user.getLevel() > oldLevel;

        return AwardResult.success(
                points,
                newTotal,
                user.getLevel(),
                levelUp,
                savedTx.getUuid()
        );
    }
}
