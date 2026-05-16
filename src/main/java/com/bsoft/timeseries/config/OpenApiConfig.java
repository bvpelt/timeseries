package com.bsoft.timeseries.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String API_KEY_SCHEME = "ApiKeyAuth";
    private static final String BEARER_SCHEME = "bearerAuth";
    @Value("${openapi.timeseries.base-path}")
    private String timeseriesBasepath;


    // ─── Security scheme names ────────────────────────────────────────────────
    @Value("${openapi.login.base-path}")
    private String loginBasepath;
    @Value("${openapi.auth.base-path}")
    private String authBasepath;

    // ─── Global OpenAPI bean ──────────────────────────────────────────────────

    @Bean
    public OpenAPI globalOpenApiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Timeseries Service")
                        .description("Bitemporal person, address and agreement management")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BSoft Development Team")))

                // Register both security schemes so the Authorize button works
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-Key")
                                        .description("API key — READ, READ_WRITE or ADMIN"))
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtained from /login")))

                // Apply both globally — individual operations can override
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }

    // ─── Groups ───────────────────────────────────────────────────────────────

    @Bean
    public GroupedOpenApi timeseriesApi() {
        return GroupedOpenApi.builder()
                .group("timeseries")
                .displayName("Bitemporal Person-Address-Agreement API")
                .pathsToMatch(timeseriesBasepath + "/**")
                .build();
    }

    @Bean
    public GroupedOpenApi loginApi() {
        return GroupedOpenApi.builder()
                .group("login")
                .displayName("Login API")
                .pathsToMatch(loginBasepath + "/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authenticationApi() {
        return GroupedOpenApi.builder()
                .group("authentication")
                .displayName("Authentication API")
                .pathsToMatch(authBasepath + "/**")
                .build();
    }
}