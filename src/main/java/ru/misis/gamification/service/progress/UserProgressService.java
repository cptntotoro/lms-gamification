package ru.misis.gamification.service.progress;

import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.dto.web.response.UserDto;

/**
 * Фасад для получения готовых DTO прогресса пользователя.
 * Используется виджетом, админ-панелью и другими местами.
 */
public interface UserProgressService {

    /**
     * Получает DTO с данными прогресса пользователя для виджета или админ-страницы
     * <p>
     * Метод:
     * <ul>
     *     <li>находит пользователя по внешнему идентификатору</li>
     *     <li>рассчитывает очки до следующего уровня</li>
     *     <li>вычисляет процент прогресса</li>
     *     <li>формирует готовый DTO для фронтенда</li>
     * </ul>
     * </p>
     *
     * @param userId Идентификатор пользователя из LMS (не может быть null или пустым)
     * @return DTO пользователя для виджета
     * @throws IllegalArgumentException если userId null или пустой
     * @throws UserNotFoundException    если пользователь не найден в системе
     * @throws IllegalStateException    если уровень пользователя некорректен (например, отрицательный)
     */
    UserDto getProgress(String userId);

    /**
     * Получает расширенный DTO для админ-панели с прогрессом.
     * <p>Включает дополнительные поля (uuid, даты создания/обновления).</p>
     *
     * @param userId внешний ID пользователя из LMS
     * @return UserAdminDto с рассчитанным прогрессом
     * @throws UserNotFoundException если пользователь не найден
     */
    UserAdminDto getAdminProgress(String userId);
}