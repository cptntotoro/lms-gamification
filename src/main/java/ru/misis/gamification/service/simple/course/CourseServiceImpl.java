package ru.misis.gamification.service.simple.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.exception.CourseNotFoundException;
import ru.misis.gamification.repository.CourseRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
public class CourseServiceImpl implements CourseService {

    /**
     * Репозиторий курсов
     */
    private final CourseRepository courseRepository;

    @Override
    public boolean existsByCourseId(@NotBlank(message = "{course.id.required}") String courseId) {
        return courseRepository.existsByCourseId(courseId);
    }

    @Override
    public UUID getCourseUuidByExternalId(@NotBlank(message = "{course.id.required}") String courseId) {
        return courseRepository.findUuidByCourseId(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    @Override
    public Course findByCourseId(@NotBlank(message = "{course.id.required}") String courseId) {
        return courseRepository.findByCourseId(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    @Override
    public Course findById(@NotNull(message = "{course.uuid.required}") UUID courseUuid) {
        return courseRepository.findById(courseUuid).orElseThrow(() -> new CourseNotFoundException(courseUuid));
    }

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Transactional
    @Override
    public Course addCourse(String courseId) {
        return courseRepository.saveAndFlush(Course.builder().courseId(courseId).build());
    }
}
