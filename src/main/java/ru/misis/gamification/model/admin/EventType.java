package ru.misis.gamification.model.admin;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.misis.gamification.dto.lms.request.LmsEventRequestDto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Тип события из LMS, определяющий сколько очков начисляется за событие этого типа.
 * <p>
 * Администратор может создавать, редактировать и удалять типы событий.
 * Каждый тип имеет уникальный код (например: "quiz", "lab", "essay").
 */
@Entity
@Table(name = "event_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventType {

    /**
     * Идентификатор записи в таблице
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID uuid;

    /**
     * Уникальный код типа события
     * Используется LMS для указания типа в запросе {@link LmsEventRequestDto#getEventType()}
     * Примеры: "quiz", "lab", "homework", "attendance", "project"
     */
    @NotBlank(message = "Код типа события обязателен")
    @Column(nullable = false, unique = true, length = 50)
    private String typeCode;

    /**
     * Название для отображения
     */
    @NotBlank(message = "Название типа события обязательно")
    @Column(nullable = false, length = 100)
    private String displayName;

    /**
     * Количество очков, начисляемых за одно событие этого типа
     */
    @NotNull(message = "Количество очков обязательно")
    @Min(value = 1, message = "Очки должны быть положительным числом")
    @Column(nullable = false)
    private Integer points;

    /**
     * Максимальное количество очков в день по этому типу (null = без ограничения)
     */
    @Min(value = 0, message = "Максимум в день должен быть ≥ 0")
    @Column
    private Integer maxDailyPoints;

    /**
     * Активен ли тип (можно отключать без удаления)
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Дата создания записи
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата обновления записи
     */
    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}