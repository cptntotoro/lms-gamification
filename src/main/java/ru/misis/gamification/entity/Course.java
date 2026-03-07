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
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Курс (дисциплина)
 * Один курс может иметь несколько групп (потоков) {@link Group}
 */
@Entity
@Table(
        name = "courses",
        indexes = {
                @Index(name = "idx_courses_course_id", columnList = "course_id"),
                @Index(name = "idx_courses_active", columnList = "active")
        }
)
@Comment("Курсы / дисциплины (например: \"Математический анализ\", \"История России\")")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    /**
     * Идентификатор записи в таблице
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID uuid;

    /**
     * Идентификатор курса из LMS
     */
    @NotBlank
    @Comment("Идентификатор курса из LMS (строка любого формата)")
    @Column(name = "course_id", nullable = false, unique = true, length = 100)
    private String courseId;

    /**
     * Название
     */
    @Column(name = "display_name")
    private String displayName;

    /**
     * Короткое название
     */
    @Column(name = "short_name", length = 50)
    private String shortName;

    /**
     * Описание
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Флаг активности
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Дата создания записи
     */
    @Column(name = "created_at", nullable = false, updatable = false)
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
}