package ru.misis.gamification.model;

import java.util.List;

/**
 * Модель персонализированного лидерборда
 *
 * @param topEntries        Список топ записей лидерборда
 * @param currentUserEntry  Модель строки лидерборда пользователя
 * @param currentUserRank   Позиция пользователя в лидерборде
 * @param currentUserPoints Количество очков пользователя
 * @param pageNumber        Номер текущей страницы (0-based)
 * @param pageSize          Размер страницы
 * @param totalElements     Общее количество элементов во всём наборе
 * @param totalPages        Общее количество страниц
 * @param hasNext           Флаг следующей страницы
 * @param hasPrevious       Флаг предыдущейстраницы
 */
public record UserCourseGroupLeaderboardView(
        List<LeaderboardEntryView> topEntries,
        LeaderboardEntryView currentUserEntry,
        Long currentUserRank,
        Integer currentUserPoints,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}