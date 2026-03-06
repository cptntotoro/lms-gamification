package ru.misis.gamification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.misis.gamification.dto.analytics.GroupLeaderboardPageDto;
import ru.misis.gamification.dto.analytics.LeaderboardEntryDto;
import ru.misis.gamification.dto.analytics.UserCourseGroupLeaderboardDto;
import ru.misis.gamification.model.LeaderboardEntryView;
import ru.misis.gamification.model.LeaderboardPageView;
import ru.misis.gamification.model.UserCourseGroupLeaderboardView;

/**
 * Маппер лидербордов
 */
@Mapper(componentModel = "spring")
public interface LeaderboardMapper {

    /**
     * Смаппить модель строки лидерборда в элемент лидерборда группы по курсу
     *
     * @param view Модель строки лидерборда
     * @return Элемент лидерборда группы по курсу
     */
    LeaderboardEntryDto toLeaderboardEntryDto(LeaderboardEntryView view);

    /**
     * Смаппить страницу лидерборда в DTO страницы лидерборда группы по курсу
     *
     * @param view Модель страницы лидерборда
     * @return DTO страницы лидерборда группы по курсу
     */
    @Mapping(target = "content", source = "content")
    GroupLeaderboardPageDto toGroupLeaderboardPageDto(LeaderboardPageView view);

    /**
     * Смаппить персонализированный лидерборд (топ + текущий пользователь) в DTO пользователя с лидербордом по курсу
     *
     * @param view Модель персонализированного лидерборда
     * @return DTO пользователя с лидербордом по курсу
     */
    UserCourseGroupLeaderboardDto toUserCourseGroupLeaderboardDto(UserCourseGroupLeaderboardView view);
}
