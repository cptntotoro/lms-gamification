package ru.misis.gamification.service.course;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.exception.CourseNotFoundException;
import ru.misis.gamification.repository.CourseRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceUnitTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

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
    void getCourseUuidByExternalId_existingCourse_returnsUuid() {
        String courseId = "MATH-101";
        UUID expectedUuid = UUID.randomUUID();

        when(courseRepository.findUuidByCourseId(courseId)).thenReturn(Optional.of(expectedUuid));

        UUID result = courseService.getCourseUuidByExternalId(courseId);

        assertThat(result).isEqualTo(expectedUuid);
        verify(courseRepository).findUuidByCourseId(courseId);
    }

    @Test
    void getCourseUuidByExternalId_nonExistingCourse_throwsCourseNotFoundException() {
        String courseId = "UNKNOWN-999";

        when(courseRepository.findUuidByCourseId(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseUuidByExternalId(courseId))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessageContaining(courseId);

        verify(courseRepository).findUuidByCourseId(courseId);
    }

    @Test
    void findByCourseId_existingCourse_returnsCourse() {
        String courseId = "MATH-101";
        Course expectedCourse = Course.builder().courseId(courseId).displayName("Математика").build();

        when(courseRepository.findByCourseId(courseId)).thenReturn(Optional.of(expectedCourse));

        Course result = courseService.findByCourseId(courseId);

        assertThat(result.getCourseId()).isEqualTo(courseId);
        assertThat(result.getDisplayName()).isEqualTo("Математика");
        verify(courseRepository).findByCourseId(courseId);
    }

    @Test
    void findByCourseId_nonExistingCourse_throwsCourseNotFoundException() {
        String courseId = "UNKNOWN-999";

        when(courseRepository.findByCourseId(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.findByCourseId(courseId))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessageContaining(courseId);

        verify(courseRepository).findByCourseId(courseId);
    }

    @Test
    void findById_existingCourse_returnsCourse() {
        UUID courseUuid = UUID.randomUUID();
        Course expectedCourse = Course.builder().uuid(courseUuid).courseId("MATH-101").build();

        when(courseRepository.findById(courseUuid)).thenReturn(Optional.of(expectedCourse));

        Course result = courseService.findById(courseUuid);

        assertThat(result.getUuid()).isEqualTo(courseUuid);
        verify(courseRepository).findById(courseUuid);
    }

    @Test
    void findById_nonExistingCourse_throwsCourseNotFoundException() {
        UUID courseUuid = UUID.randomUUID();

        when(courseRepository.findById(courseUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.findById(courseUuid))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessageContaining(courseUuid.toString());

        verify(courseRepository).findById(courseUuid);
    }
}