//package ru.misis.gamification.service.progress;
//
//import jakarta.validation.ConstraintViolationException;
//import jakarta.validation.constraints.NotBlank;
//import ru.misis.gamification.dto.admin.response.UserAdminDto;
//import ru.misis.gamification.dto.user.response.UserDto;
//import ru.misis.gamification.exception.UserNotFoundException;
//
/// **
// * Фасад для получения готовых DTO прогресса пользователя
// */
//public interface UserProgressService {
//
//    /**
//     * Получить DTO пользователя для виджета по идентификатору пользователя из LMS
//     * <p>
//     * Метод:
//     * <ul>
//     *     <li>находит пользователя по внешнему идентификатору</li>
//     *     <li>рассчитывает очки до следующего уровня</li>
//     *     <li>вычисляет процент прогресса</li>
//     *     <li>формирует готовый DTO для фронтенда</li>
//     * </ul>
//     * </p>
//     *
//     * @param userId Идентификатор пользователя из LMS
//     * @return DTO пользователя для виджета
//     * @throws ConstraintViolationException если userId == null или пустая строка
//     * @throws UserNotFoundException        если пользователь не найден в системе
//     */
//    UserDto getProgress(@NotBlank(message = "{user.id.required}") String userId);
//
//    /**
//     * Получить DTO пользователя для администратора
//     *
//     * @param userId Идентификатор пользователя из LMS
//     * @return DTO пользователя для администратора
//     * @throws ConstraintViolationException если userId == null или пустая строка
//     * @throws UserNotFoundException        если пользователь не найден
//     */
//    UserAdminDto getAdminProgress(@NotBlank(message = "{user.id.required}") String userId);
//}