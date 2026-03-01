package ru.misis.gamification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Транзакция. Используется для аудита и отображения истории
 */
@Entity
@Table(name = "transactions")
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
    private Course course;

    /**
     * Идентификатор события из LMS
     */
    @Column(name = "event_id", nullable = false, unique = true)
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
    @Column(name = "points", nullable = false)
    private Integer points;

    /**
     * Описание события
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Дата создания записи
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
