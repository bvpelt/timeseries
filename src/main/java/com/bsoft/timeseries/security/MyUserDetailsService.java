package com.bsoft.timeseries.security;


import com.bsoft.timeseries.entity.UserEntity;
import com.bsoft.timeseries.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Primary
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public MyUserPrincipal
    loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        log.debug("loadUserByUsername check user: {}", username);
        Optional<UserEntity> userDAO = usersRepository.findByUserName(username);
        if (userDAO.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        return new MyUserPrincipal(userDAO.get());
    }
}