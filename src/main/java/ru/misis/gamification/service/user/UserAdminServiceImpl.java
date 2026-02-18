package ru.misis.gamification.service.user;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.exception.UserNotFoundException;
import ru.misis.gamification.mapper.UserMapper;
import ru.misis.gamification.model.entity.User;
import ru.misis.gamification.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAdminServiceImpl implements UserAdminService {

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    /**
     * Репозиторий пользователей
     */
    private final UserRepository userRepository;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @Override
    public UserAdminDto findByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId не может быть пустым");
        }

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + userId + " не найден"));

        log.debug("Админ запросил информацию о пользователе: userId={}", userId);

        return userMapper.userToUserAdminDto(user);
    }

    @Override
    public Page<UserAdminDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::userToUserAdminDto);
    }

}
