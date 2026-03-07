package ru.misis.gamification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
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
@Table(
        name = "event_types",
        indexes = {
                @Index(name = "idx_event_types_type_code", columnList = "type_code"),
                @Index(name = "idx_event_types_active", columnList = "active")
        }
)
@Comment("Типы событий из LMS (настраиваемые шаблоны начисления очков)")
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
     * Уникальный код типа события из LMS {@link LmsEventRequestDto#getEventType()}
     */
    @NotBlank(message = "Код типа события обязателен")
    @Column(name = "type_code", nullable = false, unique = true, length = 50)
    @Comment("Уникальный код типа события из LMS (используется LMS в поле eventType)")
    private String typeCode;

    /**
     * Название для отображения
     */
    @NotBlank(message = "Название типа события обязательно")
    @Column(name = "display_name", nullable = false, length = 100)
    @Comment("Название для отображения")
    private String displayName;

    /**
     * Количество очков, начисляемых за одно событие этого типа
     */
    @NotNull(message = "Количество очков обязательно")
    @Min(value = 1, message = "Очки должны быть положительным числом")
    @Column(nullable = false)
    @Comment("Количество очков, начисляемых за одно событие этого типа")
    private Integer points;

    /**
     * Максимальное количество очков в день по этому типу (null = без ограничения)
     */
    @Min(value = 0, message = "Максимум в день должен быть ≥ 0")
    @Column
    @Comment("Максимум очков в день по типу (NULL = без лимита)")
    private Integer maxDailyPoints;

    /**
     * Активен ли тип (можно отключать без удаления)
     */
    @Column(nullable = false)
    @Builder.Default
    @Comment("Активен ли тип (можно отключать без удаления)")
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