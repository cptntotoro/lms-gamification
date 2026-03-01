package ru.misis.gamification.service.analytics;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.dto.analytics.GroupLeaderboardPageDto;
import ru.misis.gamification.dto.analytics.LeaderboardEntryDto;
import ru.misis.gamification.repository.UserCourseEnrollmentRepository;
import ru.misis.gamification.service.course.CourseService;
import ru.misis.gamification.service.group.GroupService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Validated
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
            @NotBlank(message = "{course.id.required}") String courseId,
            @NotBlank(message = "{group.id.required}") String groupId,
            @Min(value = 0, message = "{page.non-negative}") int page,
            @Min(value = 1, message = "{size.positive}") @Max(value = 100, message = "{size.too-large}") int size
    ) {
        log.debug("Запрос лидерборда: courseId={}, groupId={}, page={}, size={}", courseId, groupId, page, size);

        UUID courseUuid = courseService.getCourseUuidByExternalId(courseId);
        UUID groupUuid = groupService.getGroupUuidByExternalIdAndCourseId(groupId, courseId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "totalPointsInCourse"));
        Page<LeaderboardEntryDto> pageResult = enrollmentRepository.findLeaderboardByCourseAndGroup(courseUuid, groupUuid, pageable);

        return GroupLeaderboardPageDto.builder().content(pageResult.getContent()).pageNumber(pageResult.getNumber()).pageSize(pageResult.getSize()).totalElements(pageResult.getTotalElements()).totalPages(pageResult.getTotalPages()).hasNext(pageResult.hasNext()).hasPrevious(pageResult.hasPrevious()).build();
    }
}