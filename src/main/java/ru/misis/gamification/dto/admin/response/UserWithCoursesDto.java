package ru.misis.gamification.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWithCoursesDto {
    private String userId;
    private List<CourseEnrollmentDto> enrollments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseEnrollmentDto {
        private String courseId;
        private String groupId;
        private Integer pointsInCourse;
        private String displayName;
    }
}