package ru.misis.gamification.service.course;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.repository.CourseRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourceServiceImpl courseService;

    @Test
    void existsByCourseId_existingCourse_returnsTrue() {
        String courseId = "MATH-101";

        when(courseRepository.existsByCourseId(courseId)).thenReturn(true);

        boolean result = courseService.existsByCourseId(courseId);

        assertThat(result).isTrue();
        verify(courseRepository).existsByCourseId(courseId);
    }

    @Test
    void existsByCourseId_nonExistingCourse_returnsFalse() {
        String courseId = "PHYS-999";

        when(courseRepository.existsByCourseId(courseId)).thenReturn(false);

        boolean result = courseService.existsByCourseId(courseId);

        assertThat(result).isFalse();
        verify(courseRepository).existsByCourseId(courseId);
    }

    @Test
    void existsByCourseId_null_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> courseService.existsByCourseId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Идентификатор курса не может быть пустым или null");
    }

    @Test
    void existsByCourseId_emptyString_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> courseService.existsByCourseId("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Идентификатор курса не может быть пустым или null");
    }

    @Test
    void existsByCourseId_blankString_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> courseService.existsByCourseId(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Идентификатор курса не может быть пустым или null");
    }
}