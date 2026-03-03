package ru.misis.gamification.service.course;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.dto.analytics.LeaderboardEntryDto;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.UserCourseEnrollmentNotFoundException;
import ru.misis.gamification.repository.UserCourseEnrollmentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCourseEnrollmentServiceUnitTest {

    @Mock
    private UserCourseEnrollmentRepository repository;

    @InjectMocks
    private UserCourseEnrollmentServiceImpl service;

    private final User user = User.builder()
            .uuid(UUID.randomUUID())
            .userId("user-123")
            .build();

    private final Course course = Course.builder()
            .uuid(UUID.randomUUID())
            .courseId("MATH-101")
            .build();

    @Test
    void isUserEnrolledInCourse_existingEnrollment_returnsTrue() {
        when(repository.existsByUserAndCourse(user, course)).thenReturn(true);

        boolean result = service.isUserEnrolledInCourse(user, course);

        assertThat(result).isTrue();
        verify(repository).existsByUserAndCourse(user, course);
    }

    @Test
    void isUserEnrolledInCourse_noEnrollment_returnsFalse() {
        when(repository.existsByUserAndCourse(user, course)).thenReturn(false);

        boolean result = service.isUserEnrolledInCourse(user, course);

        assertThat(result).isFalse();
        verify(repository).existsByUserAndCourse(user, course);
    }

    @Test
    void findByUserAndCourse_existingEnrollment_returnsEnrollment() {
        UserCourseEnrollment enrollment = UserCourseEnrollment.builder()
                .user(user)
                .course(course)
                .totalPointsInCourse(500)
                .build();

        when(repository.findByUserAndCourse(user, course)).thenReturn(Optional.of(enrollment));

        UserCourseEnrollment result = service.findByUserAndCourse(user, course);

        assertThat(result).isNotNull();
        assertThat(result.getTotalPointsInCourse()).isEqualTo(500);
        verify(repository).findByUserAndCourse(user, course);
    }

    @Test
    void findByUserAndCourse_noEnrollment_throwsUserCourseEnrollmentNotFoundException() {
        when(repository.findByUserAndCourse(user, course)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByUserAndCourse(user, course))
                .isInstanceOf(UserCourseEnrollmentNotFoundException.class)
                .hasMessageContaining(user.getUserId())
                .hasMessageContaining(course.getCourseId());

        verify(repository).findByUserAndCourse(user, course);
    }

    @Test
    void save_enrollment_returnsSavedEnrollment() {
        UserCourseEnrollment enrollment = UserCourseEnrollment.builder()
                .user(user)
                .course(course)
                .totalPointsInCourse(0)
                .build();

        UserCourseEnrollment saved = UserCourseEnrollment.builder()
                .uuid(UUID.randomUUID())
                .user(user)
                .course(course)
                .totalPointsInCourse(0)
                .build();

        when(repository.save(enrollment)).thenReturn(saved);

        UserCourseEnrollment result = service.save(enrollment);

        assertThat(result.getUuid()).isNotNull();
        assertThat(result.getTotalPointsInCourse()).isZero();
        verify(repository).save(enrollment);
    }

    @Test
    void findLeaderboardByCourseAndGroup_callsRepositoryWithCorrectParams() {
        UUID courseUuid = UUID.randomUUID();
        UUID groupUuid = UUID.randomUUID();
        Pageable pageable = PageRequest.of(1, 20);

        LeaderboardEntryDto dto = LeaderboardEntryDto.builder()
                .userUuid(UUID.randomUUID())
                .userId("test")
                .pointsInCourse(100)
                .globalLevel(3)
                .rank(5L)
                .build();

        Page<LeaderboardEntryDto> mockPage = new PageImpl<>(List.of(dto), pageable, 1);

        when(repository.findLeaderboardByCourseAndGroup(courseUuid, groupUuid, pageable))
                .thenReturn(mockPage);

        Page<LeaderboardEntryDto> result = service.findLeaderboardByCourseAndGroup(courseUuid, groupUuid, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getUserId()).isEqualTo("test");
        verify(repository).findLeaderboardByCourseAndGroup(courseUuid, groupUuid, pageable);
    }

    @Test
    void findLeaderboardByCourseAndGroup_nullGroup_callsRepositoryWithNull() {
        UUID courseUuid = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findLeaderboardByCourseAndGroup(courseUuid, null, pageable))
                .thenReturn(Page.empty(pageable));

        Page<LeaderboardEntryDto> result = service.findLeaderboardByCourseAndGroup(courseUuid, null, pageable);

        assertThat(result.isEmpty()).isTrue();
        verify(repository).findLeaderboardByCourseAndGroup(courseUuid, null, pageable);
    }

    @Test
    void getRankByPointsInCourse_callsRepositoryWithCorrectParams() {
        UUID courseUuid = UUID.randomUUID();
        UUID groupUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        when(repository.findRankByPointsInCourse(courseUuid, groupUuid, userUuid))
                .thenReturn(42L);

        Long rank = service.getRankByPointsInCourse(courseUuid, groupUuid, userUuid);

        assertThat(rank).isEqualTo(42L);
        verify(repository).findRankByPointsInCourse(courseUuid, groupUuid, userUuid);
    }

    @Test
    void getRankByPointsInCourse_nullGroup_callsRepositoryWithNull() {
        UUID courseUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        when(repository.findRankByPointsInCourse(courseUuid, null, userUuid))
                .thenReturn(7L);

        Long rank = service.getRankByPointsInCourse(courseUuid, null, userUuid);

        assertThat(rank).isEqualTo(7L);
        verify(repository).findRankByPointsInCourse(courseUuid, null, userUuid);
    }
}