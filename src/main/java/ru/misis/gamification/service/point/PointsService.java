package ru.misis.gamification.service.point;

import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.dto.AwardResult;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;

public interface PointsService {
    AwardResult awardPoints(LmsEventRequestDto request);
}
