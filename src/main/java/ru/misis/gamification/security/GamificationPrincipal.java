package ru.misis.gamification.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class GamificationPrincipal implements UserDetails {

    /**
     * Идентификатор пользователя из LMS
     */
    @Getter
    private final String userId;

    /**
     * Роль пользователя (STUDENT, TEACHER, METHODIST, ADMIN)
     */
    private final String role;

    public GamificationPrincipal(String userId, String role) {
        this.userId = userId;
        this.role = role != null ? role.toUpperCase() : "STUDENT";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return null; // не используем
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}