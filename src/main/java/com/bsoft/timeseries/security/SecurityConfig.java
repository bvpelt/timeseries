package com.bsoft.timeseries.security;


import com.bsoft.timeseries.jwt.AuthEntryPointJwt;

import com.bsoft.timeseries.jwt.JwtUtils;
import com.bsoft.timeseries.security.filters.ApiKeyLoggingFilter;
import com.bsoft.timeseries.security.filters.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthEntryPointJwt authEntryPoint;

    // ─── Beans ───────────────────────────────────────────────────────────────

    @Bean
    public ApiKeyLoggingFilter apiKeyLoggingFilter(
            @Lazy ApiKeyService apiKeyService,
            ObjectMapper objectMapper) {          // ← Spring auto-configures this
        return new ApiKeyLoggingFilter(apiKeyService, objectMapper);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtUtils jwtUtils,
            UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtUtils, userDetailsService);
    }
/*
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

 */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration builder) throws Exception {
        return builder.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Replaced by your real UserDetailsService (DB-backed) at runtime.
        // This bean only suppresses the auto-generated password warning.
        return new InMemoryUserDetailsManager();
    }

    // ─── Filter chain ─────────────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ApiKeyLoggingFilter apiKeyLoggingFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                // ── Stateless REST: no sessions, no CSRF ──────────────────────
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)

                // ── CORS ──────────────────────────────────────────────────────
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(
                            "http://localhost:4200",
                            "http://localhost:8080"
                    ));
                    config.setAllowedMethods(List.of("*"));
                    config.setAllowedHeaders(List.of("*"));
                    return config;
                }))

                // ── Filters: logging first, then JWT ──────────────────────────
                .addFilterBefore(apiKeyLoggingFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)

                // ── 401 handler ───────────────────────────────────────────────
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(authEntryPoint))

                // ── Authorization rules ───────────────────────────────────────
                .authorizeHttpRequests(auth -> auth

                        // Public infrastructure
                        .requestMatchers(
                                "/",
                                "/sw.js",                    // service worker
                                "/favicon.ico",              // browser favicon request
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/webjars/**",
                                "/actuator/health"
                        ).permitAll()

                        // Login endpoints — no JWT needed (they produce the token)
                        .requestMatchers("/api/v1/login/**").permitAll()

                        // Timeseries GET — open to anyone
                        .requestMatchers(HttpMethod.GET, "/api/v1/timeseries/**").permitAll()
                        // Timeseries mutations — JWT required
                        .requestMatchers(HttpMethod.POST, "/api/v1/timeseries/**").hasAnyAuthority("ALL", "APP_WRITE", "APP_MAINTENANCE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/timeseries/**").hasAnyAuthority("ALL", "APP_WRITE", "APP_MAINTENANCE")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/timeseries/**").hasAnyAuthority("ALL", "APP_WRITE", "APP_MAINTENANCE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/timeseries/**").hasAnyAuthority("ALL", "APP_WRITE", "APP_MAINTENANCE")

                        // Auth management — JWT + specific role required
                        .requestMatchers("/api/v1/auth/**").hasAnyAuthority("ALL", "APP_MAINTENANCE")

                        // Actuator
                        .requestMatchers("/actuator/**", "/admin/api-keys").permitAll() //.hasAuthority("ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}