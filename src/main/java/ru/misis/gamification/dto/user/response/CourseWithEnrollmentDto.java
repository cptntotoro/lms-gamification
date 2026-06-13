package ru.misis.gamification.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO информации о курсе с признаком записи пользователя")
public class CourseWithEnrollmentDto {
    @Schema(description = "Идентификатор курса из LMS", example = "MATH-101")
    private String courseId;

    @Schema(description = "Отображаемое название курса")
    private String displayName;

    @Schema(description = "Записан ли пользователь на этот курс")
    private boolean enrolled;

    @Schema(description = "Количество очков пользователя на этом курсе (0, если не записан)")
    private Integer totalPointsInCourse;
}