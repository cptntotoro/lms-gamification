package ru.misis.gamification.model.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
     * Идентификатор пользователя из LMS
     */
    @Column(name = "user_id", nullable = false, length = 100)
    String userId;

    /**
     * Идентификатор события из LMS (защита от дублей)
     */
    @Column(name = "event_id", nullable = false, unique = true)
    String eventId;

    /**
     * Количество начисленных очков
     */
    @Column(name = "points", nullable = false)
    Integer pointsEarned;

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
        createdAt = LocalDateTime.now();
    }
}
