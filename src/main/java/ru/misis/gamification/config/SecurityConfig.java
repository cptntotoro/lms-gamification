package ru.misis.gamification.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.misis.gamification.security.GamificationAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public GamificationAuthenticationFilter authenticationFilter() {
        return new GamificationAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth

                        // Демо-страницы для виджетов (можно сделать публичными)
                        .requestMatchers("/demo/**").permitAll()

                        // Студенческие эндпоинты — только владелец данных
                        .requestMatchers("/api/v1/me/**").hasAnyRole("STUDENT", "TEACHER", "METHODIST", "ADMIN")

                        // Эндпоинт приёма событий от LMS
                        // TODO: дополнительная проверка подписи
                        .requestMatchers("/api/v1/event").permitAll()

                        // --- API для аутентифицированных пользователей (STUDENT, TEACHER, METHODIST, ADMIN) ---
                        // Получение данных пользователя (с проверкой владельца в самом контроллере через @PreAuthorize)
                        .requestMatchers("/api/v1/users/*").hasAnyRole("STUDENT", "TEACHER", "METHODIST", "ADMIN")
                        .requestMatchers("/api/v1/leaderboard/users/*/courses").hasAnyRole("STUDENT", "TEACHER", "METHODIST", "ADMIN")
                        .requestMatchers("/api/v1/leaderboard/course/*/user/*").hasAnyRole("STUDENT", "TEACHER", "METHODIST", "ADMIN")

                        // Студенческие эндпоинты /me (только владелец данных, проверка через @PreAuthorize)
                        .requestMatchers("/api/v1/me/**").hasAnyRole("STUDENT", "TEACHER", "METHODIST", "ADMIN")

                        // --- Преподавательские эндпоинты (если будут реализованы) ---
                        .requestMatchers("/api/v1/course/*/leaderboard").hasAnyRole("TEACHER", "METHODIST", "ADMIN")
                        .requestMatchers("/api/v1/course/*/students/*").hasAnyRole("TEACHER", "METHODIST", "ADMIN")
                        .requestMatchers("/api/v1/course/*/manual-points").hasAnyRole("TEACHER", "METHODIST", "ADMIN")

                        // --- Административные эндпоинты (только ADMIN) ---
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/analytics/**").hasRole("ADMIN")

                        // --- Документация и мониторинг ---
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**").hasRole("ADMIN")
                        .requestMatchers("/v3/api-docs/**").hasRole("ADMIN")
                        .requestMatchers("/api/actuator/**").hasRole("ADMIN")

                        // Остальное пока открыто (можно ужесточить позже)
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}