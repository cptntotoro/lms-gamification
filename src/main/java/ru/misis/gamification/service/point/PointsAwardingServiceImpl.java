//package ru.misis.gamification.service.point;
//
//import jakarta.validation.constraints.NotNull;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.validation.annotation.Validated;
//import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
//import ru.misis.gamification.entity.Course;
//import ru.misis.gamification.entity.EventType;
//import ru.misis.gamification.entity.Transaction;
//import ru.misis.gamification.entity.User;
//import ru.misis.gamification.exception.EventTypeNotFoundException;
//import ru.misis.gamification.service.simple.course.CourseService;
//import ru.misis.gamification.service.course.UserCourseService;
//import ru.misis.gamification.service.event.EventTypeService;
//import ru.misis.gamification.service.point.result.AwardResult;
//import ru.misis.gamification.service.progress.LevelCalculatorService;
//import ru.misis.gamification.service.simple.transaction.TransactionService;
//import ru.misis.gamification.service.simple.user.UserService;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@Validated
//public class PointsAwardingServiceImpl implements PointsAwardingService {
//
//    /**
//     * Сервис управления транзакциями
//     */
//    private final TransactionService transactionService;
//
//    /**
//     * Сервис управления типами событий
//     */
//    private final EventTypeService eventTypeService;
//
//    /**
//     * Сервис управления пользователями
//     */
//    private final UserService userService;
//
//    /**
//     * Сервис расчета уровня пользователя на основе накопленных очков
//     */
//    private final LevelCalculatorService levelCalculatorService;
//
//    /**
//     * Сервис управления курсами пользователей
//     */
//    private final UserCourseService userCourseService;
//
//    /**
//     * Сервис управления курсами
//     */
//    private final CourseService courseService;
//
//    @Transactional
//    @Override
//    public AwardResult awardPoints(@NotNull(message = "{request.required}") LmsEventRequestDto request) {
//        String userId = request.getUserId();
//        String eventId = request.getEventId();
//        String typeCode = request.getEventType();
//        String courseId = request.getCourseId();
//        String groupId = request.getGroupId();
//
//        if (userId == null || userId.trim().isEmpty()) {
//            log.warn("Попытка начисления без userId");
//            return AwardResult.rejected("Отсутствует идентификатор пользователя");
//        }
//
//        // 1. Проверка дубля по eventId
//        if (transactionService.isExistsByEventId(eventId)) {
//            log.info("Дубликат события: {}", eventId);
//            return AwardResult.duplicate();
//        }
//
//        // 2. Получаем активный тип события
//        EventType eventType;
//        try {
//            eventType = eventTypeService.getActiveByCode(typeCode);
//        } catch (EventTypeNotFoundException e) {
//            log.warn("Не найден активный тип события: {}", typeCode);
//            return AwardResult.rejected("Неизвестный или отключённый тип события: " + typeCode);
//        }
//
//        // 3. Получаем или создаём пользователя
//        User user = userService.createIfNotExists(userId, courseId, groupId);
//
//        // 4. Проверка дневного лимита по типу события
//        long todaySum = transactionService.sumPointsByUserAndEventTypeAndDate(
//                user.getUuid(), eventType.getUuid(), LocalDate.now());
//
//        int points = eventType.getPoints();
//
//        if (eventType.getMaxDailyPoints() != null && todaySum + points > eventType.getMaxDailyPoints()) {
//            String reason = "Превышен дневной лимит по типу " + eventType.getDisplayName();
//            log.warn(reason);
//            return AwardResult.rejected(reason);
//        }
//
//        // 5. Получаем курс, если указан
//        Course course = null;
//        if (courseId != null && !courseId.trim().isEmpty()) {
//            course = courseService.findByCourseId(courseId);
//        }
//
//        // 6. Создаём транзакцию
//        Transaction tx = Transaction.builder()
//                .user(user)
//                .course(course)  // может быть null
//                .eventType(eventType)
//                .eventId(eventId)
//                .points(points)
//                .description("Начисление за " + eventType.getDisplayName())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        Transaction savedTx = transactionService.saveIfNotExists(tx);
//
//        // 7. Обновление общего количества очков и уровня пользователя
//        int oldLevel = user.getLevel();
//        int newTotal = user.getTotalPoints() + points;
//        user.setTotalPoints(newTotal);
//        user.setLevel(levelCalculatorService.calculateLevel(newTotal));
//        userService.update(user);
//
//        boolean levelUp = user.getLevel() > oldLevel;
//
//        // 8. Начисление очков именно по курсу (если курс известен)
//        if (course != null) {
//            userCourseService.addPointsToCourse(user, course.getUuid(), points);
//        }
//
//        log.info("Начисление успешно: {} очков пользователю {}, новый уровень = {}, levelUp = {}",
//                points, userId, user.getLevel(), levelUp);
//
//        return AwardResult.success(
//                points,
//                newTotal,
//                user.getLevel(),
//                levelUp,
//                savedTx.getUuid(),
//                0L,
//                0.0
//        );
//    }
//}