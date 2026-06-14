package ru.misis.gamification.service.application.awarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.entity.Group;
import ru.misis.gamification.entity.Transaction;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.exception.DuplicateEventException;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.model.AwardResultView;
import ru.misis.gamification.model.AwardResultViews;
import ru.misis.gamification.model.EnrollmentResult;
import ru.misis.gamification.service.application.enrollment.EnrollmentApplicationService;
import ru.misis.gamification.service.simple.course.CourseService;
import ru.misis.gamification.service.simple.eventtype.EventTypeService;
import ru.misis.gamification.service.simple.group.GroupService;
import ru.misis.gamification.service.simple.progress.LevelCalculatorService;
import ru.misis.gamification.service.simple.transaction.TransactionService;
import ru.misis.gamification.service.simple.user.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@Validated
public class AwardingOrchestratorApplicationServiceImpl implements AwardingOrchestratorApplicationService {

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
    private final LevelCalculatorService levelCalculator;

    /**
     * Сервис управления курсами
     */
    private final CourseService courseService;

    /**
     * Сервис управления группами
     */
    private final GroupService groupService;

    /**
     * Фасадный сервис управления зачислениями пользователей на курсы и в группы
     */
    private final EnrollmentApplicationService enrollmentApplicationService;

    @Override
    public AwardResultView awardPoints(String userId, String eventId, String typeCode,
                                       String courseId, String groupId) {

        if (userId == null || userId.trim().isEmpty()) {
            return AwardResultViews.rejected("Отсутствует идентификатор пользователя");
        }

        if (transactionService.isExistsByEventId(eventId)) {
            log.info("Дубликат события: {}", eventId);
            return AwardResultViews.duplicate();
        }

        EventType eventType;
        try {
            eventType = eventTypeService.getActiveByCode(typeCode);
        } catch (EventTypeNotFoundException e) {
            return AwardResultViews.rejected("Неизвестный или отключённый тип события: " + typeCode);
        }

        User user = userService.createIfNotExists(userId);
        EnrollmentResult enrollmentResult = enrollmentApplicationService.enrollIfNeeded(userId, courseId, groupId);

        Course course = enrollmentResult.course();
        if (course == null) {
            return AwardResultViews.rejected("Не указан курс пользователя");
        }

        Group group = enrollmentResult.group();
        if (group == null && groupId != null && !groupId.trim().isEmpty() && course != null) {
            UUID groupUuid = groupService.getGroupUuidByExternalIdAndCourseId(groupId, courseId);
            group = groupService.findById(groupUuid);
        }

        if(group != null && !enrollmentApplicationService.isUserInGroup(user, group)) {
            return AwardResultViews.rejected("Пользователь уже состоит в другой группе курса: " + courseId);
        }

        long todaySum = transactionService.sumPointsByUserAndEventTypeAndDate(
                user.getUuid(), eventType.getUuid(), LocalDate.now());

        int points = eventType.getPoints();
        if (eventType.getMaxDailyPoints() != null && todaySum + points > eventType.getMaxDailyPoints()) {
            return AwardResultViews.rejected("Превышен дневной лимит по типу " + eventType.getDisplayName());
        }

        Transaction tx = Transaction.builder()
                .user(user)
                .course(course)
                .group(group)
                .eventType(eventType)
                .eventId(eventId)
                .points(points)
                .description("Начисление за " + eventType.getDisplayName())
                .createdAt(LocalDateTime.now())
                .build();

        try {
            transactionService.saveIfNotExists(tx);
        } catch (DuplicateEventException e) {
            return AwardResultViews.rejected("Событие уже было обработано: " + eventId);
        }

        int oldLevel = user.getLevel();
        int newTotal = user.getTotalPoints() + points;
        user.setTotalPoints(newTotal);
        user.setLevel(levelCalculator.calculateLevel(newTotal));
        userService.update(user);

        boolean levelUp = user.getLevel() > oldLevel;

        if (course != null) {
            enrollmentApplicationService.addPointsToCourse(userId, course.getUuid(), points);
        }

        log.info("Начисление успешно: {} очков пользователю {}, новый уровень = {}", points, userId, user.getLevel());

        long pointsToNext = levelCalculator.pointsToNextLevel(user.getLevel());
        double progress = pointsToNext > 0 ? Math.min((double) newTotal / pointsToNext * 100, 100) : 100.0;

        return AwardResultViews.success(points, newTotal, levelUp, user.getLevel(), pointsToNext, progress);
    }
}
