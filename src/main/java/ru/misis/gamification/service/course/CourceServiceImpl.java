package ru.misis.gamification.service.course;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.repository.CourseRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourceServiceImpl implements CourseService {

    /**
     * Репозиторий курсов
     */
    private final CourseRepository courseRepository;

    @Override
    public boolean existsByCourseId(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Идентификатор курса не может быть пустым или null");
        }
        return courseRepository.existsByCourseId(courseId);
    }
}
