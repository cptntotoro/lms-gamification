package ru.misis.gamification.service.simple.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.Group;
import ru.misis.gamification.exception.GroupNotFoundException;
import ru.misis.gamification.repository.GroupRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class GroupServiceImpl implements GroupService {

    /**
     * Репозиторий групп
     */
    private final GroupRepository groupRepository;

    @Override
    public boolean existsByGroupIdAndCourseId(@NotBlank(message = "{group.id.required}") String groupId,
                                              @NotBlank(message = "{course.id.required}") String courseId) {
        return groupRepository.existsByGroupIdAndCourseCourseId(groupId, courseId);
    }

    @Override
    public UUID getGroupUuidByExternalIdAndCourseId(@NotBlank(message = "{group.id.required}") String groupId,
                                                    @NotBlank(message = "{course.id.required}") String courseId) {
        return groupRepository.findUuidByGroupIdAndCourseCourseId(groupId, courseId)
                .orElseThrow(() -> new GroupNotFoundException(groupId, courseId));
    }

    @Override
    public Group findById(@NotNull(message = "{group.uuid.required}") UUID groupUuid) {
        return groupRepository.findById(groupUuid)
                .orElseThrow(() -> new GroupNotFoundException(groupUuid));
    }
}
