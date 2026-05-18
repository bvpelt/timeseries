package com.bsoft.timeseries.service;

import com.bsoft.timeseries.authentication.model.Role;
import com.bsoft.timeseries.authentication.model.User;
import com.bsoft.timeseries.authentication.model.UserBody;
import com.bsoft.timeseries.entity.RolesEntity;
import com.bsoft.timeseries.entity.UserEntity;
import com.bsoft.timeseries.exception.UserExistsException;
import com.bsoft.timeseries.exception.UserNotExistsException;
import com.bsoft.timeseries.mapper.UserMapper;
import com.bsoft.timeseries.repository.RoleRepository;
import com.bsoft.timeseries.repository.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UsersService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder; // = new BCryptPasswordEncoder();

    public void deleteAll() {
        try {
            usersRepository.deleteAll();
        } catch (Exception e) {
            log.error("Deleting all users failed: {}", e.toString());
        }
    }

    public boolean deleteUser(Long userId) {
        boolean deleted = false;
        try {
            Optional<UserEntity> optionalUser = usersRepository.findByUserId(userId);
            if (optionalUser.isEmpty()) {
                throw new UserNotExistsException("User with id " + userId + " not found and not deleted");
            }
            usersRepository.deleteById(userId);
            deleted = true;
        } catch (Exception e) {
            log.error("Delete user failed: {}", e.toString());
            throw e;
        }
        return deleted;
    }

    public User getUser(Long userId) {
        Optional<UserEntity> optionalUser = usersRepository.findByUserId(userId);
        if (optionalUser.isEmpty()) {
            throw new UserNotExistsException("User with id " + userId + " not found");
        }

        UserEntity userEntity = optionalUser.get();

        return userMapper.map(userEntity);
    }

    public User getUserByName(String username) {
        Optional<UserEntity> optionalUser = usersRepository.findByUserName(username);
        if (optionalUser.isEmpty()) {
            throw new UserNotExistsException("User with name " + username + " not found");
        } else {
            UserEntity userEntity = optionalUser.get();
            if (!userEntity.getEnabled()) {
                throw new UserNotExistsException("User not enabled");
            }
            if (!userEntity.getAccount_non_expired()) {
                throw new UserNotExistsException("User not expired");
            }
            if (!userEntity.getAccount_non_locked()) {
                throw new UserNotExistsException("User locked");
            }
            if (!userEntity.getCredentials_non_expired()) {
                throw new UserNotExistsException("User credentials expired");
            }
        }

        UserEntity userEntity = optionalUser.get();

        return userMapper.map(userEntity);
    }

    public List<User> getUsers() {
        List<User> userList = new ArrayList<User>();
        Iterable<UserEntity> userIterable = usersRepository.findAll();

        userIterable.forEach(userInstance -> {
            User user = userMapper.map(userInstance);
            userList.add(user);
        });

        return userList;
    }

    public Page<User> getUsersPage(PageRequest pageRequest) {
        Page<UserEntity> foundUsersPage = usersRepository.findAllByPage(pageRequest);

        List<User> usersList = new ArrayList<>();
        usersList = foundUsersPage.getContent().stream()
                .map(userMapper::map) // Apply mapper to each User
                .toList();

        return new PageImpl<>(usersList, foundUsersPage.getPageable(), foundUsersPage.getTotalElements());
    }

    public User postUser(Boolean override, final UserBody userBody) {
        UserEntity userEntity = new UserEntity(userBody);

        try {
            if (!override) {
                Optional<UserEntity> optionalUser = usersRepository.findByHash(userEntity.getHash());
                if (optionalUser.isPresent()) {
                    throw new UserExistsException("User " + userEntity + " already exists cannot insert again");
                }
            }

            String pwd = userBody.getPassword();
            String encodedPwd = passwordEncoder.encode(pwd);
            userEntity.setPassword(encodedPwd);
            userEntity.genHash();

            usersRepository.save(userEntity);

            return userMapper.map(userEntity); // Return 201 Created with the created entity
        } catch (Error e) {
            log.error("Error inserting adres: {}", e.toString());
            throw e;
        }
    }

    public User patch(Long userId, final UserBody userBody) {

        try {
            Optional<UserEntity> optionalUser = usersRepository.findByUserId(userId);
            if (optionalUser.isEmpty()) {
                throw new UserNotExistsException("User with id " + userId + " not found");
            }
            UserEntity foundUser = optionalUser.get();
            if (userBody.getEmail() != null) {
                foundUser.setEmail(userBody.getEmail());
            }
            if (userBody.getUsername() != null) {
                foundUser.setUsername(userBody.getUsername());
            }
            if (userBody.getPassword() != null) {
                String encodedPwd = passwordEncoder.encode(userBody.getPassword());
                foundUser.setPassword(encodedPwd);
            }
            if (userBody.getPhone() != null) {
                foundUser.setPhone(userBody.getPhone());
            }
            if (userBody.getAccountNonExpired() != null) {
                foundUser.setAccount_non_expired(userBody.getAccountNonExpired());
            }
            if (userBody.getAccountNonLocked() != null) {
                foundUser.setAccount_non_locked(userBody.getAccountNonLocked());
            }
            if (userBody.getCredentialsNonExpired() != null) {
                foundUser.setCredentials_non_expired(userBody.getCredentialsNonExpired());
            }
            if (userBody.getEnabled() != null) {
                foundUser.setEnabled(userBody.getEnabled());
            }

            // set new hash -- assumption is at least one field is changed, no need to check!
            foundUser.setHash(foundUser.genHash());

            UserEntity savedUser = usersRepository.save(foundUser);
            updateUserRoles(savedUser, userBody);

            return userMapper.map(savedUser);
        } catch (Error e) {
            log.error("Error patching adres: {}", e.toString());
            throw e;
        }

    }

    private void updateUserRoles(UserEntity savedUser, UserBody userBody) {
        List<Role> currentRoles = userBody.getRoles();
        Collection<RolesEntity> foundRoles = savedUser.getRoles();

        // Extract IDs from both sides for comparison
        Set<Long> currentRoleIds = currentRoles.stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        Set<Long> foundRoleIds = foundRoles.stream()
                .map(RolesEntity::getId)
                .collect(Collectors.toSet());

        // Only update if the sets actually differ
        if (!currentRoleIds.equals(foundRoleIds)) {
            log.debug("updateUserRoles - roles changed: found={} current={}", foundRoleIds, currentRoleIds);

            // Verify all requested roles exist in the database — fail fast if not
            List<RolesEntity> resolvedRoles = currentRoleIds.stream()
                    .map(id -> roleRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Role not found with id: " + id)))
                    .toList();

            // Replace the collection in-place so JPA tracks the change
            savedUser.getRoles().clear();
            savedUser.getRoles().addAll(resolvedRoles);

            usersRepository.save(savedUser);
            log.debug("updateUserRoles - updated roles for user={} to={}",
                    savedUser.getUsername(), currentRoleIds);
        } else {
            log.debug("updateUserRoles - roles unchanged for user={}", savedUser.getUsername());
        }
    }

}