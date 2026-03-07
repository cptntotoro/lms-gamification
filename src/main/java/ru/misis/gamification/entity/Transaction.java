package ru.misis.gamification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Транзакция. Используется для аудита и отображения истории
 */
@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_transactions_user_uuid", columnList = "user_uuid"),
                @Index(name = "idx_transactions_course_uuid", columnList = "course_uuid"),
                @Index(name = "idx_transactions_event_type_uuid", columnList = "event_type_uuid"),
                @Index(name = "idx_transactions_event_id", columnList = "event_id"),
                @Index(name = "idx_transactions_created_at", columnList = "created_at DESC"),
                @Index(name = "idx_transactions_user_created", columnList = "user_uuid, created_at DESC")
        }
)
@Comment("Транзакции")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /**
     * Идентификатор записи в таблице
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID uuid;

    /**
     * Пользователь
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", nullable = false)
    private User user;

    /**
     * Курс
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_uuid")
    @Comment("Ссылка на курс (для аналитики)")
    private Course course;

    /**
     * Идентификатор события из LMS
     */
    @NotBlank
    @Column(name = "event_id", nullable = false, unique = true)
    @Comment("Идентификатор события из LMS")
    private String eventId;

    /**
     * Тип события
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_uuid", nullable = false)
    private EventType eventType;

    /**
     * Количество начисленных очков
     */
    @NotNull
    @Column(name = "points", nullable = false)
    @Comment("Количество начисленных очков")
    private Integer points;

    /**
     * Описание события
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Дата создания записи
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
