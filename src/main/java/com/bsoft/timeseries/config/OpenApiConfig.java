package com.bsoft.timeseries.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures SpringDoc to expose the hand-crafted OpenAPI YAML file
 * and registers the API-key security scheme so the Swagger UI
 * "Authorize" button works out of the box.
 */
@Configuration
public class OpenApiConfig implements WebMvcConfigurer {

    @Value("${openapi.timeseries.base-path}")
    private String timeseriesBasepath;

    @Value("${openapi.login.base-path}")
    private String loginBasepath;

    @Value("${openapi.auth.base-path}")
    private String authBasepath;

    @Bean
    public GroupedOpenApi timeseriesApi() {
        return GroupedOpenApi.builder()
                .group("timeseries")
                .displayName("Bitemporal Person-Address-Agreement API")
                .pathsToMatch(timeseriesBasepath + "/**")   // matches all timeseries endpoints
                .build();
    }

    @Bean
    public GroupedOpenApi loginApi() {
        return GroupedOpenApi.builder()
                .group("login")
                .displayName("Login API")
                .pathsToMatch(loginBasepath + "/**")   // adjust to match your login.yaml paths
                .build();
    }

    @Bean
    public GroupedOpenApi authenticationApi() {
        return GroupedOpenApi.builder()
                .group("authentication")
                .displayName("Authentication API")
                .pathsToMatch(authBasepath + "/**")    // adjust to match your authentication.yaml paths
                .build();
    }

    @Bean
    public OpenAPI globalOpenApiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Timeseries Service")
                        .description("Bitemporal person, address and agreement management")
                        .version("1.0.0"));
    }
}