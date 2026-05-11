package com.bsoft.timeseries.security;


import com.bsoft.timeseries.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class MyUserPrincipal implements UserDetails {

    private final UserEntity user;

    public MyUserPrincipal(UserEntity user) {
        this.user = user;
        log.trace("MyUserPrincipal created {}", user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        user.getRoles().forEach(role -> {

            log.trace("get role: {}", role.getRolename());
            role.getPrivileges().forEach(principle -> {
                log.trace("get principle: {}", principle.getName());
                authorities.add(new SimpleGrantedAuthority(principle.getName()));
            });
        });
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getAccount_non_expired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getAccount_non_locked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.getCredentials_non_expired();
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled();
    }
}