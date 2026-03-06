package ru.misis.gamification.service.simple.enrollment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.entity.UserCourseEnrollment;
import ru.misis.gamification.exception.UserCourseEnrollmentNotFoundException;
import ru.misis.gamification.model.LeaderboardEntryView;
import ru.misis.gamification.repository.UserCourseEnrollmentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceUnitTest {

    @Mock
    private UserCourseEnrollmentRepository repository;

    @InjectMocks
    private EnrollmentServiceImpl service;

    @Test
    void isUserEnrolledInCourse_existing_returnsTrue() {
        User user = new User();
        Course course = new Course();

        when(repository.existsByUserAndCourse(user, course)).thenReturn(true);

        boolean result = service.isUserEnrolledInCourse(user, course);

        assertThat(result).isTrue();
        verify(repository).existsByUserAndCourse(user, course);
    }

    @Test
    void isUserEnrolledInCourse_notExisting_returnsFalse() {
        User user = new User();
        Course course = new Course();

        when(repository.existsByUserAndCourse(user, course)).thenReturn(false);

        boolean result = service.isUserEnrolledInCourse(user, course);

        assertThat(result).isFalse();
        verify(repository).existsByUserAndCourse(user, course);
    }

    @Test
    void findByUserAndCourse_existing_returnsEnrollment() {
        User user = new User();
        Course course = new Course();
        UserCourseEnrollment enrollment = new UserCourseEnrollment();

        when(repository.findByUserAndCourse(user, course)).thenReturn(Optional.of(enrollment));

        UserCourseEnrollment result = service.findByUserAndCourse(user, course);

        assertThat(result).isSameAs(enrollment);
        verify(repository).findByUserAndCourse(user, course);
    }

    @Test
    void findByUserAndCourse_notFound_throwsException() {
        User user = User.builder().userId("u-123").build();
        Course course = Course.builder().courseId("c-456").build();

        when(repository.findByUserAndCourse(user, course)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByUserAndCourse(user, course))
                .isInstanceOf(UserCourseEnrollmentNotFoundException.class)
                .hasMessageContaining("u-123")
                .hasMessageContaining("c-456");

        verify(repository).findByUserAndCourse(user, course);
    }

    @Test
    void findLeaderboardByCourseAndGroup_callsRepositoryAndReturnsPage() {
        UUID courseUuid = UUID.randomUUID();
        UUID groupUuid = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        LeaderboardEntryView entry = new LeaderboardEntryView(UUID.randomUUID(), "user1", 100, 5, 1L, false);
        Page<LeaderboardEntryView> expectedPage = new PageImpl<>(List.of(entry), pageable, 1);

        when(repository.findLeaderboardByCourseAndGroup(courseUuid, groupUuid, pageable))
                .thenReturn(expectedPage);

        Page<LeaderboardEntryView> result = service.findLeaderboardByCourseAndGroup(courseUuid, groupUuid, pageable);

        assertThat(result).isSameAs(expectedPage);
        verify(repository).findLeaderboardByCourseAndGroup(courseUuid, groupUuid, pageable);
    }

    @Test
    void getRankByPointsInCourse_callsRepositoryAndReturnsValue() {
        UUID courseUuid = UUID.randomUUID();
        UUID groupUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();

        when(repository.findRankByPointsInCourse(courseUuid, groupUuid, userUuid))
                .thenReturn(42L);

        Long result = service.getRankByPointsInCourse(courseUuid, groupUuid, userUuid);

        assertThat(result).isEqualTo(42L);
        verify(repository).findRankByPointsInCourse(courseUuid, groupUuid, userUuid);
    }

    @Test
    void save_callsRepositoryAndReturnsSavedEntity() {
        UserCourseEnrollment enrollment = new UserCourseEnrollment();
        UserCourseEnrollment saved = new UserCourseEnrollment();

        when(repository.save(enrollment)).thenReturn(saved);

        UserCourseEnrollment result = service.save(enrollment);

        assertThat(result).isSameAs(saved);
        verify(repository).save(enrollment);
    }

    @Test
    void save_nullEnrollment_callsRepositoryWithNull() {
        when(repository.save(null)).thenReturn(null);

        UserCourseEnrollment result = service.save(null);

        assertThat(result).isNull();
        verify(repository).save(null);
    }
}