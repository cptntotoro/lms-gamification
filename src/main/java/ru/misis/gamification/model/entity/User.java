package ru.misis.gamification.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Пользователь. Автоматически создается при первом событии от пользователя.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Идентификатор записи в таблице
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    /**
     * Идентификатор пользователя из LMS
     */
    @Column(name = "user_id", nullable = false, unique = true, length = 100)
    private String userId;

    /**
     * Общее количество очков
     */
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;

    /**
     * Уровень пользователя
     */
    @Column(name = "level", nullable = false)
    private Integer level = 1;

    /**
     * Дата создания записи
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Дата обновления записи
     */
    @Column(name = "updated_at")
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

    /**
     * Пересчет уровня по формуле: level = floor(totalPoints / 100) + 1
     */
    public void recalculateLevel() {
        this.level = (this.totalPoints / 100) + 1;
    }
}
