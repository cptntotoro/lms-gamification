package ru.misis.gamification.service.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.dto.admin.response.UserAdminDto;
import ru.misis.gamification.entity.User;
import ru.misis.gamification.mapper.UserMapper;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
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
    public UserAdminDto findByUserId(@NotBlank(message = "{user.id.required}") String userId) {
        User user = userService.getUserByExternalId(userId);

        log.debug("Админ успешно получил информацию о пользователе: userId={}", userId);

        return userMapper.userToUserAdminDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserAdminDto> findAll(@NotNull(message = "{pageable.required}") Pageable pageable) {
        log.debug("Админ запросил список всех пользователей: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        return userService.findAll(pageable)
                .map(userMapper::userToUserAdminDto);
    }

}
