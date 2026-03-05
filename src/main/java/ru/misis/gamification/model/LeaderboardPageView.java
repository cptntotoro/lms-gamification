package ru.misis.gamification.model;

import java.util.List;

/**
 * Модель страницы лидерборда
 *
 * @param content       Список строк лидерборда на текущей странице
 * @param pageNumber    Номер текущей страницы (0-based)
 * @param pageSize      Размер страницы
 * @param totalElements Общее количество элементов во всём наборе
 * @param totalPages    Общее количество страниц
 * @param hasNext       Флаг следующей страницы
 * @param hasPrevious   Флаг предыдущейстраницы
 */
public record LeaderboardPageView(
        List<LeaderboardEntryView> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
