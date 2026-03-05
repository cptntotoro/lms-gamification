//package ru.misis.gamification.service.point;
//
//import jakarta.validation.constraints.NotNull;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.validation.annotation.Validated;
//import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
//import ru.misis.gamification.entity.User;
//import ru.misis.gamification.service.point.result.AwardResult;
//import ru.misis.gamification.service.progress.calculator.ProgressCalculator;
//import ru.misis.gamification.service.progress.result.ProgressMetrics;
//import ru.misis.gamification.service.simple.user.UserService;
//
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//@Slf4j
//@Validated
//public class PointsServiceImpl implements PointsService {
//
//    /**
//     * Сервис бизнес-логики начисления очков за событие из LMS
//     */
//    private final PointsAwardingService pointsAwardingService;
//
//    /**
//     * Компонент расчёта метрик прогресса пользователя
//     */
//    private final ProgressCalculator progressCalculator;
//
//    /**
//     * Сервис управления пользователями
//     */
//    private final UserService userService;
//
//    @Override
//    public AwardResult awardPoints(@NotNull(message = "{request.required}") LmsEventRequestDto request) {
//        log.debug("Начало начисления очков: userId={}, eventId={}, typeCode={}",
//                request.getUserId(), request.getEventId(), request.getEventType());
//
//        AwardResult result = pointsAwardingService.awardPoints(request);
//
//        if (result.isSuccess()) {
//            UUID userUuid = userService.getUserUuidByExternalId(request.getUserId());
//            User user = userService.getByUuid(userUuid);
//
//            ProgressMetrics metrics = progressCalculator.calculate(user);
//
//            return AwardResult.success(
//                    result.getPointsEarned(),
//                    result.getTotalPointsAfter(),
//                    result.getNewLevel(),
//                    result.isLevelUp(),
//                    result.getTransactionId(),
//                    metrics.getPointsToNextLevel(),
//                    metrics.getProgressPercent()
//            );
//        }
//
//        return result;
//    }
//}
