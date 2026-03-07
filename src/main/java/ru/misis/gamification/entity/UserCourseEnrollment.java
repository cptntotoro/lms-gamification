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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Зачисление на курс (связь пользователь — курс)
 */
@Entity
@Table(
        name = "user_course_enrollments",
        uniqueConstraints = @UniqueConstraint(name = "unique_user_course", columnNames = {"user_uuid", "course_uuid"}),
        indexes = {
                @Index(name = "idx_enrollments_user_course", columnList = "user_uuid, course_uuid"),
                @Index(name = "idx_enrollments_course_id", columnList = "course_uuid"),
                @Index(name = "idx_enrollments_course_group_points", columnList = "course_uuid, group_uuid, total_points_in_course DESC")
        }
)
@Comment("Зачисление студентов на курсы + статистика по курсу")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCourseEnrollment {

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
    @Comment("Ссылка на пользователя")
    private User user;

    /**
     * Курс (дисциплина)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_uuid", nullable = false)
    @Comment("Ссылка на курс")
    private Course course;

    /**
     * Группа / поток внутри курса (опционально)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_uuid")
    @Comment("Группа/поток (может быть NULL)")
    private Group group;

    /**
     * Всего очков за курс
     */
    // TODO: везде заменить на Long
    @Builder.Default
    @Column(name = "total_points_in_course", nullable = false)
    @Comment("Сумма очков, заработанных именно на этом курсе")
    private Integer totalPointsInCourse = 0;

    /**
     * Дата записи на курс
     */
    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    /**
     * Дата завершения курса (опционально)
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        enrolledAt = LocalDateTime.now();
    }
}