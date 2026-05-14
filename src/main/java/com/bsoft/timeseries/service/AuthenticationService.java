package com.bsoft.timeseries.service;


import com.bsoft.timeseries.authentication.model.User;
import com.bsoft.timeseries.entity.RolesEntity;
import com.bsoft.timeseries.entity.UserEntity;
import com.bsoft.timeseries.exception.InvalidUserException;
import com.bsoft.timeseries.exception.UserExistsException;
import com.bsoft.timeseries.jwt.JwtUtils;
import com.bsoft.timeseries.login.model.LoginRequest;
import com.bsoft.timeseries.login.model.LoginResponse;
import com.bsoft.timeseries.repository.RoleRepository;
import com.bsoft.timeseries.repository.UsersRepository;
import com.bsoft.timeseries.security.MyUserPrincipal;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Slf4j
@Service

public class AuthenticationService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UsersService usersService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public LoginResponse register(LoginRequest request) {
        log.debug("AuthenticationService register - authenticationresponse for request: {}", request.toString());

        Optional<RolesEntity> optionalRoleDAO = roleRepository.findByRolename("USER");
        RolesEntity defRole = null;

        if (optionalRoleDAO.isPresent()) {
            defRole = optionalRoleDAO.get();
        } else {
            RolesEntity rolesDTO = new RolesEntity();
            rolesDTO.setRolename("JWT-TOKEN");
            rolesDTO.setDescription("Using JWT token authentication");
            rolesDTO.genHash();
            defRole = roleRepository.save(rolesDTO);
        }

        var user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        defRole.addUser(user);
        user.getRoles().add(defRole);
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.genHash();
        try {
            usersRepository.save(user);
        } catch (Exception e) {
            log.error("AuthenticationService register - User: {} not saved", user.getUsername());
            throw new UserExistsException("User already exists based on username or email");
        }

        MyUserPrincipal myUserPrincipal = new MyUserPrincipal(user);

        var jwtToken = jwtUtils.generateTokenFromUsername(myUserPrincipal);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setAuthenticated(true);

        log.trace("AuthenticationService register - generated token: {}", jwtUtils.decodeToken(jwtToken));

        return loginResponse;
    }

    public LoginResponse authenticate(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", request.getUsername());
            throw new InvalidUserException("Authentication failed for user: " + request.getUsername());
        }

        var user = usersRepository
                .findByUserName(request.getUsername())
                .orElseThrow();

        MyUserPrincipal myUserPrincipal = new MyUserPrincipal(user);

        var jwtToken = jwtUtils.generateTokenFromUsername(myUserPrincipal);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setAuthenticated(true);

        log.trace("AuthenticationService authenticate - generated token: {}", jwtUtils.decodeToken(jwtToken));

        return loginResponse;
    }

    public LoginResponse basicjwt(LoginRequest loginRequest) {
        log.debug("AuthenticationService basicjwt - loginRequest: {}", loginRequest.toString());

        LoginResponse loginResponse = new LoginResponse();

        User user = usersService.getUserByName(loginRequest.getUsername());

        Boolean authenticated = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        if (authenticated) {
            MyUserPrincipal myUserPrincipal = new MyUserPrincipal(new UserEntity(user));

            var jwtToken = jwtUtils.generateTokenFromUsername(myUserPrincipal);
            log.debug("AuthenticationService basicjwt - generated token: {}", jwtUtils.decodeToken(jwtToken));
            loginResponse.setToken(jwtToken);
        }

        loginResponse.setAuthenticated(authenticated);

        return loginResponse;
    }
}