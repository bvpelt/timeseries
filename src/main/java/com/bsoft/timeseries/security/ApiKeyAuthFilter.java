package com.bsoft.timeseries.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Servlet filter that extracts the {@code X-API-Key} header, resolves the key
 * to a {@link ApiKeyPermission}, and populates the Spring Security context.
 *
 * <p>Requests without a valid key receive {@code 401 Unauthorized}.
 * Requests with a key of permission {@code NO_ACCESS} receive {@code 403 Forbidden}.
 * These checks happen before Spring Security's own access-decision logic,
 * which enforces per-endpoint HTTP method restrictions.</p>
 */
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";

    private final ApiKeyService apiKeyService;

    // Handmatige constructor, GEEN @RequiredArgsConstructor
    public ApiKeyAuthFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Swagger UI and OpenAPI spec docs are always accessible
        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String rawKey = request.getHeader(API_KEY_HEADER);
        if (rawKey == null || rawKey.isBlank()) {
            log.debug("Request rejected — missing {} header: {}", API_KEY_HEADER, path);
            writeError(response, HttpStatus.UNAUTHORIZED, "Missing API key");
            return;
        }

        Optional<ApiKeyPermission> permissionOpt = apiKeyService.resolve(rawKey);
        if (permissionOpt.isEmpty()) {
            log.debug("Request rejected — unknown/inactive API key for: {}", path);
            writeError(response, HttpStatus.UNAUTHORIZED, "Invalid or expired API key");
            return;
        }

        ApiKeyPermission permission = permissionOpt.get();
        if (permission == ApiKeyPermission.NO_ACCESS) {
            log.debug("Request rejected — NO_ACCESS key for: {}", path);
            writeError(response, HttpStatus.FORBIDDEN, "API key has been revoked");
            return;
        }

        // Populate the security context so downstream Spring Security rules apply
        ApiKeyAuthentication auth = new ApiKeyAuthentication(rawKey, permission);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    // ---------------------------------------------------------------

    private boolean isPublicPath(String path) {
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api-docs")
                || path.equals("/actuator/health");
    }

    private void writeError(HttpServletResponse response,
                            HttpStatus status,
                            String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = """
                {"status":%d,"error":"%s","message":"%s","timestamp":"%s"}
                """.formatted(status.value(), status.getReasonPhrase(),
                message, OffsetDateTime.now()).strip();
        response.getWriter().write(body);
    }
}