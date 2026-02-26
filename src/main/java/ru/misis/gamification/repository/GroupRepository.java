package ru.misis.gamification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.misis.gamification.model.entity.Course;
import ru.misis.gamification.model.entity.Group;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий групп
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {

    /**
     * Получить группу по внешнему идентификатору группы из LMS и курсу
     *
     * @param groupId Идентификатор группы из LMS
     * @return Optional с группой или пустой, если не найден
     */
    Optional<Group> findByGroupIdAndCourse(String groupId, Course course);
}