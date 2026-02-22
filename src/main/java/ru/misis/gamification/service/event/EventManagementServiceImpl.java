package ru.misis.gamification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.dto.AwardResult;
import ru.misis.gamification.dto.AwardStatus;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.dto.lms.response.LmsEventResponsetDto;
import ru.misis.gamification.exception.EventTypeNotFoundException;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.service.event.EventManagementService;
import ru.misis.gamification.service.event.EventTypeService;
import ru.misis.gamification.service.point.PointsService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventManagementServiceImpl implements EventManagementService {

    private final PointsService pointsAwardService;
    private final EventTypeService eventTypeService;

    @Override
    @Transactional
    public LmsEventResponsetDto process(LmsEventRequestDto request) {
        log.info("Получено событие от LMS: userId={}, eventId={}, type={}",
                request.getUserId(), request.getEventId(), request.getEventType());

        // Пытаемся начислить очки через основной сервис
        AwardResult result;
        try {
            result = pointsAwardService.awardPoints(request);
        } catch (EventTypeNotFoundException e) {
            log.warn("Не найден активный тип события: {}", request.getEventType());
            return LmsEventResponsetDto.error("Неизвестный или отключённый тип события");
        } catch (Exception e) {
            log.error("Критическая ошибка при начислении очков", e);
            return LmsEventResponsetDto.error("Внутренняя ошибка сервера");
        }

        // Формируем ответ в зависимости от результата
        if (result.isSuccess()) {
            log.info("Успешно начислено {} очков → userId={}, total={}, level={}, levelUp={}",
                    result.getPointsEarned(),
                    request.getUserId(),
                    result.getTotalPointsAfter(),
                    result.getLevelAfter(),
                    result.isLevelUp());

            LmsEventResponsetDto response = LmsEventResponsetDto.success(
                    request.getUserId(),
                    result.getPointsEarned(),
                    result.getTotalPointsAfter(),
                    request.getEventId(),
                    result.getTransactionId(),
                    "Начислено за событие" // или подтянуть displayName
            );

            response.setLevelUp(result.isLevelUp());

            return response;
        }

        if (result.getStatus() == AwardStatus.DUPLICATE) {
            log.info("Дубликат события: {}", request.getEventId());
            return LmsEventResponsetDto.duplicate(request.getEventId());
        }

        log.warn("Начисление отклонено: {}", result.getRejectionReason());
        return LmsEventResponsetDto.error(result.getRejectionReason());
    }
}