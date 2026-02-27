package ru.misis.gamification.service.user;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.mapper.UserMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAdminServiceImpl implements UserAdminService {

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

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

        User user = userService.get(userId);

        log.debug("Админ успешно получил информацию о пользователе: userId={}", userId);
        
        return userMapper.userToUserAdminDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserAdminDto> findAll(Pageable pageable) {
        log.debug("Админ запросил список всех пользователей: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        return userService.findAll(pageable)
                .map(userMapper::userToUserAdminDto);
    }

}
