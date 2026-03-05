package ru.misis.gamification.service.application.awarding;

import ru.misis.gamification.model.AwardResultView;

public interface AwardingOrchestratorApplicationService {

    /**
     * Начислить очки пользователю за событие от LMS
     *
     * @param userId   Идентификатор пользователя из LMS
     * @param eventId  Идентификатор события из LMS
     * @param typeCode Уникальный код типа события из LMS
     * @param courseId Идентификатор курса из LMS
     * @param groupId  Идентификатор группы из LMS
     * @return Результат операции начисления очков по событию из LMS (успех, дубликат, отклонение и т.д.)
     */
    AwardResultView awardPoints(String userId, String eventId, String typeCode,
                                String courseId, String groupId);
}
