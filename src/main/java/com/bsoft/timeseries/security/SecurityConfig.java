package com.bsoft.timeseries.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public ApiKeyAuthFilter apiKeyAuthFilter(@Lazy ApiKeyService apiKeyService) {
        return new ApiKeyAuthFilter(apiKeyService);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(); // suppresses auto-generated password
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ApiKeyAuthFilter apiKeyAuthFilter)
            throws Exception {
        http
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/webjars/**",
                                "/actuator/metrics/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/**")
                        .hasAnyAuthority("ADMIN", "READ", "READ_WRITE")
                        .requestMatchers(HttpMethod.POST, "/api/v1/**").hasAnyAuthority("ADMIN", "READ_WRITE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/**").hasAnyAuthority("ADMIN", "READ_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasAnyAuthority("ADMIN", "READ_WRITE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/**").hasAnyAuthority("ADMIN", "READ_WRITE")
                        .requestMatchers(HttpMethod.POST, "/actuator/**", "/admin/api-keys").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}