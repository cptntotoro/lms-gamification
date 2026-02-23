package ru.misis.gamification.service.level;

/**
 * Сервис расчета уровней
 */
public interface LevelCalculatorService {

    /**
     * Рассчитать уровень
     *
     * @param totalPoints Всего очков
     * @return Уровень
     */
    int calculateLevel(int totalPoints);

    /**
     * Получить количество очков до следующего уровня
     *
     * @param currentLevel Текущий уровень
     * @return Количество очков до следующего уровня
     */
    long pointsToNextLevel(int currentLevel);
}
