package ru.misis.gamification.service.application.awarding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.dto.lms.response.LmsEventResponseDto;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.model.AwardResultView;
import ru.misis.gamification.service.simple.eventtype.EventTypeService;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@Validated
public class LmsEventProcessorApplicationServiceImpl implements LmsEventProcessorApplicationService {

    /**
     * Сервис-ркестратор начисления баллов для события из LMS
     */
    private final AwardingOrchestratorApplicationService awardingOrchestrator;

    /**
     * Сервис управления типами событий
     */
    private final EventTypeService eventTypeService;

    @Override
    public LmsEventResponseDto process(LmsEventRequestDto request) {
        log.debug("Обработка события от LMS: userId={}, eventId={}, type={}",
                request.getUserId(), request.getEventId(), request.getEventType());

        AwardResultView result = awardingOrchestrator.awardPoints(
                request.getUserId(), request.getEventId(), request.getEventType(),
                request.getCourseId(), request.getGroupId());

        if (result.success()) {
            EventType type = eventTypeService.getActiveByCode(request.getEventType());

            return LmsEventResponseDto.success(
                    request.getUserId(),
                    result.pointsEarned(),
                    result.totalPointsAfter(),
                    result.levelUp(),
                    result.newLevel(),
                    result.pointsToNextLevel(),
                    result.progressPercent(),
                    request.getEventId(),
                    type.getDisplayName()
            );
        }

        if (result.duplicate()) {
            return LmsEventResponseDto.duplicate(request.getEventId());
        }

        return LmsEventResponseDto.error(result.rejectionReason());
    }
}
