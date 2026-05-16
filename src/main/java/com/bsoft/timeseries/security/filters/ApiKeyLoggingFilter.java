package com.bsoft.timeseries.security.filters;


import com.bsoft.timeseries.security.ApiKeyService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ApiKeyLoggingFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_PATH_PREFIX = "/api/v";

    private final ApiKeyService apiKeyService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String key = request.getHeader(API_KEY_HEADER);
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String ip = resolveClientIp(request);
        boolean isApiRequest = uri.startsWith(API_PATH_PREFIX);  // ← correct check

        if (key == null || key.isBlank()) {
            if (isApiRequest) {
                // API key is required for /api/v* — reject with 401
                log.warn("ApiKeyLoggingFilter - missing API key - method={} uri={} ip={}",
                        method, uri, ip);
                writeErrorResponse(response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Missing API key",
                        "X-API-Key header is required for " + uri);
                return;  // ← stop the filter chain, do not call chain.doFilter()
            } else {
                // Non-API path (swagger, actuator etc.) — log and continue
                log.trace("ApiKeyLoggingFilter - no API key (non-API path) - method={} uri={} ip={}",
                        method, uri, ip);
            }
        } else {
            Optional<String> owner = apiKeyService.resolveOwner(key);
            if (owner.isPresent()) {
                log.info("ApiKeyLoggingFilter - method={} uri={} ip={} key={} owner={}",
                        method, uri, ip, key, owner.get());
            } else {
                if (isApiRequest) {
                    // Unknown/inactive key on an API path — reject with 401
                    log.warn("ApiKeyLoggingFilter - unknown/inactive API key - method={} uri={} ip={} key={}",
                            method, uri, ip, key);
                    writeErrorResponse(response,
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Invalid API key",
                            "The provided X-API-Key is unknown or inactive");
                    return;  // ← stop the filter chain
                } else {
                    log.warn("ApiKeyLoggingFilter - unknown/inactive key (non-API path) - method={} uri={} ip={}",
                            method, uri, ip);
                }
            }
        }

        // Continue the filter chain for all permitted cases
        chain.doFilter(request, response);
    }

    private void writeErrorResponse(HttpServletResponse response,
                                    int status,
                                    String error,
                                    String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "status", status,
                "error", error,
                "message", message,
                "timestamp", OffsetDateTime.now().toString()
        ));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}