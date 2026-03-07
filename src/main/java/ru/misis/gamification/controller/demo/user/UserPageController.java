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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.misis.gamification.dto.user.response.UserGlobalCourseGroupDto;
import ru.misis.gamification.dto.user.response.UserStatisticsDto;
import ru.misis.gamification.mapper.UserMapper;
import ru.misis.gamification.model.UserProgressView;
import ru.misis.gamification.model.UserStatisticsView;
import ru.misis.gamification.service.application.user.UserProgressApplicationService;
import ru.misis.gamification.service.application.user.UserStatisticsApplicationService;

@Tag(name = "Web Pages", description = "Простые HTML-страницы приложения")
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

    @Operation(
            summary = "Данные для виджета пользователя",
            description = """
                    Возвращает прогресс пользователя в контексте курса и (опционально) группы.
                    courseId — обязательный параметр.
                    groupId — опциональный (если передан, то возвращаются также данные по группе).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешно получены данные для виджета",
                    content = @Content(schema = @Schema(implementation = UserStatisticsDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные входные параметры"),
            @ApiResponse(responseCode = "404", description = "Пользователь / курс / группа / зачисление не найдены")
    })
    @GetMapping("/{userId}")
    public String getUserWidget(
            @PathVariable
            @Parameter(description = "Внешний ID пользователя из LMS", example = "stud-98765")
            String userId,

            @RequestParam(required = false)
            @Parameter(description = "Внешний идентификатор курса", example = "CS-101-2025")
            String courseId,

            @RequestParam(required = false)
            @Parameter(description = "Внешний идентификатор группы/потока", example = "G-14")
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
}
