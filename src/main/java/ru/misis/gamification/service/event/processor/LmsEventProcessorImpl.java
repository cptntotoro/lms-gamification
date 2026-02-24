package ru.misis.gamification.service.event.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.dto.lms.response.LmsEventResponseDto;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.service.event.EventTypeService;
import ru.misis.gamification.service.point.PointsService;
import ru.misis.gamification.service.point.result.AwardResult;
import ru.misis.gamification.service.point.result.AwardStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class LmsEventProcessorImpl implements LmsEventProcessor {

    /**
     * Сервис начисления очков
     */
    private final PointsService pointsService;

    /**
     * Сервис управления типами событий
     */
    private final EventTypeService eventTypeService;

    @Override
    @Transactional
    public LmsEventResponseDto process(LmsEventRequestDto request) {
        log.debug("Обработка события от LMS: userId={}, eventId={}, type={}",
                request.getUserId(), request.getEventId(), request.getEventType());

        AwardResult result = pointsService.awardPoints(request);

        if (result.isSuccess()) {
            EventType type = eventTypeService.getActiveByCode(request.getEventType());

            return LmsEventResponseDto.success(
                    request.getUserId(),
                    result.getPointsEarned(),
                    result.getTotalPointsAfter(),
                    result.isLevelUp(),
                    result.getNewLevel(),
                    result.getPointsToNextLevel(),
                    result.getProgressPercent(),
                    request.getEventId(),
                    result.getTransactionId(),
                    type.getDisplayName()
            );
        }

        if (result.getStatus() == AwardStatus.DUPLICATE) {
            return LmsEventResponseDto.duplicate(request.getEventId());
        }

        return LmsEventResponseDto.error(result.getRejectionReason());
    }
}