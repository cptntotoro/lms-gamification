package ru.misis.gamification.controller.demo.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.misis.gamification.dto.admin.response.UserWithCoursesDto;
import ru.misis.gamification.dto.user.response.UserGlobalCourseGroupDto;
import ru.misis.gamification.dto.user.response.UserStatisticsDto;
import ru.misis.gamification.mapper.UserMapper;
import ru.misis.gamification.model.UserAdminView;
import ru.misis.gamification.model.UserProgressView;
import ru.misis.gamification.model.UserStatisticsView;
import ru.misis.gamification.service.application.user.UserAdminApplicationService;
import ru.misis.gamification.service.application.user.UserProgressApplicationService;
import ru.misis.gamification.service.application.user.UserStatisticsApplicationService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/demo/users")
@RequiredArgsConstructor
@Slf4j
public class UserPageController {

    /**
     * Фасадный сервис управления прогрессом очков и уровня пользователей
     */
    private final UserProgressApplicationService userProgressService;

    /**
     * Фасадный сервис управления статистикой пользователей
     */
    private final UserStatisticsApplicationService statisticsService;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    /**
     * Фасадный сервис управления пользователями для администратора
     */
    private final UserAdminApplicationService adminApplicationService;

    private final UserStatisticsApplicationService userStatisticsApplicationService;

    /**
     * Данные для виджета пользователя
     */
    @GetMapping("/{userId}")
    public String getUserWidget(
            @PathVariable
            String userId,
            @RequestParam(required = false)
            String courseId,
            @RequestParam(required = false)
            String groupId,
            Model model) {

        log.debug("Демо-виджет для userId={}, courseId={}, groupId={}", userId, courseId, groupId);

        try {
            // Всегда загружаем глобальный прогресс
            UserProgressView progress = userProgressService.getProgress(userId);
            UserGlobalCourseGroupDto userGlobalCourseGroupDto = userMapper.toUserGlobalCourseGroupDto(progress);

            model.addAttribute("user", userGlobalCourseGroupDto);
            model.addAttribute("global", true); // флаг, что это общий прогресс

            // Если передан courseId — загружаем статистику по курсу
            if (courseId != null && !courseId.trim().isEmpty()) {
                UserStatisticsView stats = statisticsService.getUserStatistics(userId, courseId, groupId);

                userGlobalCourseGroupDto = userGlobalCourseGroupDto.toBuilder()
                        .courseId(stats.courseId())
                        .groupId(stats.groupId())
                        .pointsInCourse(stats.pointsInCourse())
                        .rankInCourse(stats.rankInCourse())
                        .rankInGroup(stats.rankInGroup())
                        .build();

                model.addAttribute("stats", userGlobalCourseGroupDto);
                model.addAttribute("global", false); // переключаем на режим курса
            }

            return "widget";
        } catch (Exception e) {
            log.warn("Ошибка загрузки виджета для {}: {}", userId, e.getMessage());
            model.addAttribute("error", "Данные недоступны");
            model.addAttribute("user", null);
            return "widget";
        }
    }

    /**
     * Список всех пользователей с их курсами и группами
     */
    @GetMapping
    public ResponseEntity<List<UserWithCoursesDto>> getAllUsersWithCourses() {
        // Используем существующий метод, чтобы получить всех пользователей (без пагинации, максимум 1000)
        Pageable pageable = PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, "userId"));
        Page<UserAdminView> usersPage = adminApplicationService.findAll(null, null, pageable);

        List<UserWithCoursesDto> result = usersPage.getContent().stream()
                .map(user -> UserWithCoursesDto.builder()
                        .userId(user.userId())
                        .enrollments(userStatisticsApplicationService.getUserCourses(user.userId()).getCourses().stream()
                                .map(enr -> UserWithCoursesDto.CourseEnrollmentDto.builder()
                                        .courseId(enr.getCourseId())
                                        .groupId(enr.getGroupId())
                                        .pointsInCourse(enr.getTotalPointsInCourse())
                                        .displayName(enr.getDisplayName())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
