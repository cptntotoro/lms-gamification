package ru.misis.gamification.service.group;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.repository.GroupRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupServiceImpl implements GroupService {

    /**
     * Репозиторий групп
     */
    private final GroupRepository groupRepository;

    @Override
    public boolean existsByGroupIdAndCourseId(String groupId, String courseId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            throw new IllegalArgumentException("Идентификатор группы не может быть пустым или null");
        }
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Идентификатор курса не может быть пустым или null");
        }

        return groupRepository.existsByGroupIdAndCourseCourseId(groupId, courseId);
    }
}
