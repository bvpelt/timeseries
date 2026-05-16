package com.bsoft.timeseries.security;

import com.bsoft.timeseries.jwt.AuthEntryPointJwt;
import com.bsoft.timeseries.jwt.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig_old {

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    //@Bean
    public ApiKeyAuthFilter xapiKeyAuthFilter(@Lazy ApiKeyService apiKeyService) {
        return new ApiKeyAuthFilter(apiKeyService);
    }

    //@Bean
    public AuthTokenFilter xjwtTokenFilter() {
        return new AuthTokenFilter();
    }

    //@Bean
    public AuthenticationManager xauthenticationManager(AuthenticationConfiguration builder) throws Exception {
        return builder.getAuthenticationManager();
    }

    //@Bean
    public UserDetailsService xuserDetailsService() {
        return new InMemoryUserDetailsManager(); // suppresses auto-generated password
    }

    //@Bean
    public SecurityFilterChain xsecurityFilterChain(HttpSecurity http,
                                                    ApiKeyAuthFilter apiKeyAuthFilter)

            throws Exception {

        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:81", "http://localhost:8080", "https://editor.swagger.io/", "https://editor-next.swagger.io/"));
            config.setAllowedMethods(List.of("*")); // Allow all HTTP methods
            config.setAllowedHeaders(List.of("*")); // Allow all headers
            return config;
        }));

        http
//                .sessionManagement(sm ->
//                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .csrf(AbstractHttpConfigurer::disable)
//                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/sw.js",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/api/v1/login/**",
                                "/webjars/**",
                                "/actuator/metrics/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/timeseries/**").permitAll()
                        //.hasAnyAuthority("ADMIN", "READ", "READ_WRITE")
                        .requestMatchers(HttpMethod.POST, "/api/v1/timeseries/**").hasAnyAuthority("ADMIN", "READ_WRITE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/timeseries/**").hasAnyAuthority("ADMIN", "READ_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/timeseries/**").hasAnyAuthority("ADMIN", "READ_WRITE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/timeseries/**").hasAnyAuthority("ADMIN", "READ_WRITE")

                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/privileges").hasAnyAuthority("APP_WRITE", "APP_MAINTENANCE")
                        .requestMatchers("/actuator/**", "/admin/api-keys").permitAll() //.hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
//                .formLogin(AbstractHttpConfigurer::disable)
//                .httpBasic(AbstractHttpConfigurer::disable)
        ;

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // create new session for each request

        // add exception handler
        http.exceptionHandling(exception -> {
            exception.authenticationEntryPoint(unauthorizedHandler);
        });

        // enable basic authentication
        //      http.httpBasic(Customizer.withDefaults());

        http.headers(headers ->
                headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        http.csrf(AbstractHttpConfigurer::disable);

//        http.addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}