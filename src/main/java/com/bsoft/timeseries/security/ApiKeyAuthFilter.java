package com.bsoft.timeseries.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-Key";
    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String key = request.getHeader(HEADER);
        if (key != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            apiKeyService.resolvePermission(key).ifPresent(permission -> {
                if (!"NO_ACCESS".equals(permission)) {
                    var auth = new ApiKeyAuthentication(key,
                            ApiKeyPermission.valueOf(permission));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            });
        }
        chain.doFilter(request, response);
    }
}