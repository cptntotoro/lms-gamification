package ru.misis.gamification.service.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.dto.analytics.GroupLeaderboardPageDto;
import ru.misis.gamification.dto.analytics.LeaderboardEntryDto;
import ru.misis.gamification.exception.CourseNotFoundException;
import ru.misis.gamification.exception.GroupNotFoundException;
import ru.misis.gamification.repository.UserCourseEnrollmentRepository;
import ru.misis.gamification.service.course.CourseService;
import ru.misis.gamification.service.group.GroupService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    /**
     * Репозиторий связей пользователь — курс
     */
    private final UserCourseEnrollmentRepository enrollmentRepository;

    /**
     * Сервис управления курсами
     */
    private final CourseService courseService;

    /**
     * Сервис управления группами/потоками
     */
    private final GroupService groupService;

    @Override
    public GroupLeaderboardPageDto getGroupLeaderboard(
            String courseId,
            String groupId,
            int page,
            int size
    ) {
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Идентификатор курса не может быть пустым или null");
        }
        if (groupId == null || groupId.trim().isEmpty()) {
            throw new IllegalArgumentException("Идентификатор группы не может быть пустым или null");
        }

        log.debug("Запрос лидерборда: courseId={}, groupId={}, page={}, size={}",
                courseId, groupId, page, size);

        if (!courseService.existsByCourseId(courseId)) {
            log.warn("Курс не найден: {}", courseId);
            throw new CourseNotFoundException(courseId);
        }

        if (!groupService.existsByGroupIdAndCourseId(groupId, courseId)) {
            log.warn("Группа не найдена: groupId={}, courseId={}", groupId, courseId);
            throw new GroupNotFoundException(groupId, courseId);
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<LeaderboardEntryDto> pageResult = enrollmentRepository
                .findLeaderboardByCourseAndGroup(courseId, groupId, pageable);

        return GroupLeaderboardPageDto.builder()
                .content(pageResult.getContent())
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }
}