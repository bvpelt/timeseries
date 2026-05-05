package com.bsoft.timeseries.config;

import com.bsoft.timeseries.security.ApiKeyAuthFilter;
import com.bsoft.timeseries.security.ApiKeyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 6 configuration.
 *
 * <h2>Strategy</h2>
 * <ol>
 *   <li>Stateless — no sessions, no CSRF required (REST + API key auth).</li>
 *   <li>A custom {@link ApiKeyAuthFilter} is inserted before the standard
 *       username/password filter. It validates the {@code X-API-Key} header
 *       and populates the security context.</li>
 *   <li>HTTP-level rules enforce READ vs READ_WRITE:
 *       <ul>
 *         <li>GET requests require at least {@code READ} authority.</li>
 *         <li>POST / PUT / DELETE requests require {@code READ_WRITE} authority.</li>
 *       </ul>
 *   </li>
 *   <li>{@code @EnableMethodSecurity} allows finer-grained {@code @PreAuthorize}
 *       annotations on individual service methods if needed.</li>
 * </ol>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public ApiKeyAuthFilter apiKeyAuthFilter(@Lazy ApiKeyService apiKeyService) {
        return new ApiKeyAuthFilter(apiKeyService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ApiKeyAuthFilter apiKeyAuthFilter)
            throws Exception {
        http
                // REST API — no sessions, no CSRF
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)

                // Register the API key filter before username/password processing
                .addFilterBefore(apiKeyAuthFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth

                        // Public endpoints: Swagger UI, OpenAPI spec, health check
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/actuator/health"
                        ).permitAll()

                        // Read-only operations: READ or READ_WRITE
                        .requestMatchers(HttpMethod.GET, "/api/v1/**")
                        .hasAnyAuthority("READ", "READ_WRITE")

                        // Mutating operations: READ_WRITE only
                        .requestMatchers(HttpMethod.POST, "/api/v1/**")
                        .hasAuthority("READ_WRITE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/**")
                        .hasAuthority("READ_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**")
                        .hasAuthority("READ_WRITE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/**")
                        .hasAuthority("READ_WRITE")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Disable default form login and HTTP Basic — we use API keys only
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}