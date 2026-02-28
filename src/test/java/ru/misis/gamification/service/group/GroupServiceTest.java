package ru.misis.gamification.service.group;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.repository.GroupRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupServiceImpl groupService;

    @Test
    void existsByGroupIdAndCourseId_existingGroup_returnsTrue() {
        String groupId = "PM-21-1";
        String courseId = "MATH-101";

        when(groupRepository.existsByGroupIdAndCourseCourseId(groupId, courseId))
                .thenReturn(true);

        boolean result = groupService.existsByGroupIdAndCourseId(groupId, courseId);

        assertThat(result).isTrue();
        verify(groupRepository).existsByGroupIdAndCourseCourseId(groupId, courseId);
    }

    @Test
    void existsByGroupIdAndCourseId_nonExistingGroup_returnsFalse() {
        String groupId = "PM-99-9";
        String courseId = "MATH-101";

        when(groupRepository.existsByGroupIdAndCourseCourseId(groupId, courseId))
                .thenReturn(false);

        boolean result = groupService.existsByGroupIdAndCourseId(groupId, courseId);

        assertThat(result).isFalse();
        verify(groupRepository).existsByGroupIdAndCourseCourseId(groupId, courseId);
    }

    @Test
    void existsByGroupIdAndCourseId_nullGroupId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> groupService.existsByGroupIdAndCourseId(null, "MATH-101"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Идентификатор группы не может быть пустым или null");
    }

    @Test
    void existsByGroupIdAndCourseId_emptyGroupId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> groupService.existsByGroupIdAndCourseId("   ", "MATH-101"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Идентификатор группы не может быть пустым или null");
    }

    @Test
    void existsByGroupIdAndCourseId_nullCourseId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> groupService.existsByGroupIdAndCourseId("PM-21-1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Идентификатор курса не может быть пустым или null");
    }

    @Test
    void existsByGroupIdAndCourseId_emptyCourseId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> groupService.existsByGroupIdAndCourseId("PM-21-1", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Идентификатор курса не может быть пустым или null");
    }
}