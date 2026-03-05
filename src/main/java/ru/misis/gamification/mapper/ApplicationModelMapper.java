package ru.misis.gamification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import ru.misis.gamification.dto.admin.request.EventTypeCreateDto;
import ru.misis.gamification.dto.admin.request.EventTypeUpdateDto;
import ru.misis.gamification.dto.admin.response.EventTypeDto;
import ru.misis.gamification.dto.admin.response.TransactionItemDto;
import ru.misis.gamification.dto.admin.response.TransactionPageDto;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.dto.analytics.GroupLeaderboardPageDto;
import ru.misis.gamification.dto.analytics.LeaderboardEntryDto;
import ru.misis.gamification.dto.analytics.UserCourseGroupLeaderboardDto;
import ru.misis.gamification.dto.user.response.UserDto;
import ru.misis.gamification.dto.user.response.UserStatisticsDto;
import ru.misis.gamification.entity.EventType;
import ru.misis.gamification.model.EventTypeSummary;
import ru.misis.gamification.model.LeaderboardEntryView;
import ru.misis.gamification.model.LeaderboardPageView;
import ru.misis.gamification.model.TransactionSummary;
import ru.misis.gamification.model.UserAdminView;
import ru.misis.gamification.model.UserCourseGroupLeaderboardView;
import ru.misis.gamification.model.UserProgressView;
import ru.misis.gamification.model.UserStatisticsView;

/**
 * Маппер моделей в DTO
 */
@Mapper(componentModel = "spring")
public interface ApplicationModelMapper {

    /**
     * Смаппить модель прогресса пользователя в DTO пользователя
     *
     * @param view Модель прогресса пользователя
     * @return DTO пользователя
     */
    UserDto toUserDto(UserProgressView view);

    /**
     * Смаппить модель пользователя для админ-панели в соответствующий DTO
     *
     * @param view Модель пользователя для админ-панели
     * @return DTO пользователя для администратора
     */
    UserAdminDto toUserAdminDto(UserAdminView view);

    /**
     * Смаппить модель статистики пользователя по курсу/группе в DTO
     *
     * @param view Модель статистики пользователя
     * @return DTO пользователя в контексте курса и (опционально) группы
     */
    UserStatisticsDto toUserStatisticsDto(UserStatisticsView view);

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

    /**
     * Смаппить модель типа события в DTO типа события для администратора
     *
     * @param summary Модель типа события
     * @return DTO типа события для администратора
     */
    EventTypeDto toEventTypeDto(EventTypeSummary summary);

    /**
     * Смаппить модель транзакции в DTO транзакции для администратора
     *
     * @param summary Модель транзакции
     * @return DTO транзакции для администратора
     */
    TransactionItemDto toTransactionItemDto(TransactionSummary summary);

    /**
     * Смаппить страницу модели транзакции в DTO страницы транзакций для администратора
     *
     * @param page Страница модели транзакции
     * @return DTO страницы транзакций для администратора
     */
    @Mapping(target = "content", source = "content")
    @Mapping(target = "pageNumber", source = "number")
    @Mapping(target = "pageSize", source = "size")
    @Mapping(target = "totalElements", source = "totalElements")
    @Mapping(target = "totalPages", source = "totalPages")
    @Mapping(target = "hasNext", expression = "java(page.hasNext())")
    @Mapping(target = "hasPrevious", expression = "java(page.hasPrevious())")
    TransactionPageDto toTransactionPageDto(Page<TransactionSummary> page);

    /**
     * Смаппить страницу модели транзакции в DTO страницы транзакций для администратора
     *
     * @param page    Страница модели транзакции
     * @param ignored
     * @return DTO страницы транзакций для администратора
     */
    default TransactionPageDto toTransactionPageDto(Page<TransactionSummary> page, boolean ignored) {
        return toTransactionPageDto(page);
    }

    /**
     * Смаппить DTO создания типа события в тип события
     *
     * @param eventTypeCreateDto DTO создания типа события
     * @return Тип события
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventType eventTypeCreateDtoToEventType(EventTypeCreateDto eventTypeCreateDto);

    /**
     * Смаппить DTO обновления типа события в тип события
     *
     * @param eventTypeUpdateDto DTO обновления типа события
     * @return Тип события
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "typeCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventType eventTypeUpdateDtoToEventType(EventTypeUpdateDto eventTypeUpdateDto);
}
