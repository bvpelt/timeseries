package com.bsoft.timeseries.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures SpringDoc to expose the hand-crafted OpenAPI YAML file
 * and registers the API-key security scheme so the Swagger UI
 * "Authorize" button works out of the box.
 */
@Configuration
public class OpenApiConfig implements WebMvcConfigurer {

    /**
     * Makes the OpenAPI YAML available at {@code /api-docs/openapi.yaml}
     * so it can be referenced from {@code springdoc.swagger-ui.url} in
     * application.yml.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api-docs/**")
                .addResourceLocations("classpath:/openapi/");
    }

    /**
     * Programmatic OpenAPI customisation: registers the API-key security
     * scheme that SpringDoc generates into the live {@code /v3/api-docs}
     * endpoint (separate from the hand-crafted YAML).
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "ApiKeyAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Bitemporal Person-Address-Agreement Service")
                        .version("1.0.0")
                        .description("Full bitemporal CRUD service following Snodgrass (1999)"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name("X-API-Key")
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .description("API key — header: X-API-Key")));
    }
}