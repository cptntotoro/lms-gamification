package ru.misis.gamification.service.group;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.Group;
import ru.misis.gamification.exception.GroupNotFoundException;
import ru.misis.gamification.repository.GroupRepository;
import ru.misis.gamification.service.simple.group.GroupServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceUnitTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupServiceImpl groupService;

    private final String groupId = "PM-21-1";
    private final String courseId = "MATH-101";
    private final UUID groupUuid = UUID.randomUUID();
    private final UUID courseUuid = UUID.randomUUID();

    @Test
    void existsByGroupIdAndCourseId_existingGroup_returnsTrue() {
        when(groupRepository.existsByGroupIdAndCourseCourseId(groupId, courseId))
                .thenReturn(true);

        boolean result = groupService.existsByGroupIdAndCourseId(groupId, courseId);

        assertThat(result).isTrue();
        verify(groupRepository).existsByGroupIdAndCourseCourseId(groupId, courseId);
    }

    @Test
    void existsByGroupIdAndCourseId_nonExistingGroup_returnsFalse() {
        when(groupRepository.existsByGroupIdAndCourseCourseId(groupId, courseId))
                .thenReturn(false);

        boolean result = groupService.existsByGroupIdAndCourseId(groupId, courseId);

        assertThat(result).isFalse();
        verify(groupRepository).existsByGroupIdAndCourseCourseId(groupId, courseId);
    }

    @Test
    void getGroupUuidByExternalIdAndCourseId_existingGroup_returnsUuid() {
        when(groupRepository.findUuidByGroupIdAndCourseCourseId(groupId, courseId))
                .thenReturn(Optional.of(groupUuid));

        UUID result = groupService.getGroupUuidByExternalIdAndCourseId(groupId, courseId);

        assertThat(result).isEqualTo(groupUuid);
        verify(groupRepository).findUuidByGroupIdAndCourseCourseId(groupId, courseId);
    }

    @Test
    void getGroupUuidByExternalIdAndCourseId_nonExistingGroup_throwsGroupNotFoundException() {
        when(groupRepository.findUuidByGroupIdAndCourseCourseId(groupId, courseId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.getGroupUuidByExternalIdAndCourseId(groupId, courseId))
                .isInstanceOf(GroupNotFoundException.class)
                .hasMessageContaining(groupId)
                .hasMessageContaining(courseId);

        verify(groupRepository).findUuidByGroupIdAndCourseCourseId(groupId, courseId);
    }

    @Test
    void findById_existingGroup_returnsGroup() {
        Group group = Group.builder()
                .uuid(groupUuid)
                .groupId(groupId)
                .displayName("Поток ПМ-21-1")
                .course(Course.builder().uuid(courseUuid).courseId(courseId).build())
                .active(true)
                .build();

        when(groupRepository.findById(groupUuid)).thenReturn(Optional.of(group));

        Group result = groupService.findById(groupUuid);

        assertThat(result.getUuid()).isEqualTo(groupUuid);
        assertThat(result.getGroupId()).isEqualTo(groupId);
        verify(groupRepository).findById(groupUuid);
    }

    @Test
    void findById_nonExistingGroup_throwsGroupNotFoundException() {
        when(groupRepository.findById(groupUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.findById(groupUuid))
                .isInstanceOf(GroupNotFoundException.class)
                .hasMessageContaining(groupUuid.toString());

        verify(groupRepository).findById(groupUuid);
    }
}