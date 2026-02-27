package ru.misis.gamification.service.group;

/**
 * Сервис управления группами/потоками
 */
public interface GroupService {

    /**
     * Проверить существование группы по её внешнему идентификатору из LMS внутри указанного курса
     *
     * @param groupId  Идентификатор группы из LMS
     * @param courseId @param courseId Идентификатор курса из LMS
     * @return Да / Нет
     * @throws IllegalArgumentException если groupId или courseId null или пустые
     */
    boolean existsByGroupIdAndCourseId(String groupId, String courseId);
}
