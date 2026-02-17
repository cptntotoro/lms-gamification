package ru.misis.gamification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения для сервиса геймификации.
 * <p>
 * Запускает Spring Boot приложение с встроенным сервером Tomcat.
 * Приложение предоставляет REST API для приема событий от LMS,
 * начисления очков пользователям и отображения прогресса.
 * </p>
 *
 * @version 1.0
 */
@SpringBootApplication
public class GamificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamificationApplication.class, args);
    }
}