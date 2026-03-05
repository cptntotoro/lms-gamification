package ru.misis.gamification.model;

import java.util.UUID;

/**
 * Модель пользователя
 *
 * @param uuid        UUID пользователя
 * @param userId      Идентификатор пользователя из LMS
 * @param totalPoints Общее количество накопленных очков
 * @param level       Текущий уровень
 */
public record UserSummary(UUID uuid, String userId, Integer totalPoints, Integer level) {
}
