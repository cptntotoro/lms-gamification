package ru.misis.gamification.service.level;

public interface LevelCalculator {
    int calculateLevel(int totalPoints);

    long pointsToNextLevel(int currentLevel);
}
