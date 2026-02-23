package ru.misis.gamification.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.dto.AwardResult;
import ru.misis.gamification.dto.AwardStatus;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;
import ru.misis.gamification.dto.lms.response.LmsEventResponsetDto;
import ru.misis.gamification.model.admin.EventType;
import ru.misis.gamification.service.point.PointsService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventManagementServiceImpl implements EventManagementService {

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
    public LmsEventResponsetDto process(LmsEventRequestDto request) {
        AwardResult result = pointsService.awardPoints(request);

        if (result.isSuccess()) {
            EventType type = eventTypeService.getActiveByCode(request.getEventType());

            return LmsEventResponsetDto.builder()
                    .status("success")
                    .userId(request.getUserId())
                    .eventId(request.getEventId())
                    .displayName(type.getDisplayName())
                    .pointsEarned(result.getPointsEarned())
                    .totalPoints(result.getTotalPointsAfter())
                    .pointsToNextLevel(result.getPointsToNextLevel())
                    .transactionId(result.getTransactionId())
                    .processedAt(LocalDateTime.now())
                    .levelUp(result.isLevelUp())
                    .build();
        }

        if (result.getStatus() == AwardStatus.DUPLICATE) {
            return LmsEventResponsetDto.duplicate(request.getEventId());
        }

        return LmsEventResponsetDto.error(result.getRejectionReason());
    }
}