package com.bsoft.timeseries.jwt;


import com.bsoft.timeseries.security.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Value("${openapi.timeseries.base-path}")
    private String timeseriesBasepath;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException {
        log.trace("AuthTokenFilter - doFilterInternal - Called for method: {} URI: {}", request.getMethod(), request.getRequestURI());

        if (!request.getRequestURI().isEmpty()) {
            try {
                checkAPIKey(request);

                String jwt = parseJwt(request);
                if (jwt != null && jwtUtils.validateToken(jwt)) {
                    String username = jwtUtils.getUsernameFromToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                            null,
                            userDetails.getAuthorities());
                    log.trace("AuthTokenFilter - doFiterInternal - Roles from JWT: {}", userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Authenticating the user for the duration of the request
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.error("AuthTokenFilter - doFilterInternal - Cannot set user authentication: {}", e.toString());
                throw new ServletException(e);
            }
            try {
                filterChain.doFilter(request, response);
            } catch (ServletException e) {
                log.error("AuthTokenFilter - doFilterInternal - ServletException caught in filterchain: {}", e.toString());
                throw new RuntimeException(e);
            } catch (Exception e) {
                log.error("AuthTokenFilter - doFilterInternal - Exception caught in filterchain: {}", e.toString());
                throw new RuntimeException(e);
            }
        } else {
            log.error("AuthTokenFilter - doFilterInternal - Request URI is empty");
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String jwt = jwtUtils.getJwtFromHeader(request);
        log.trace("AuthTokenFilter - parseJwt - Called for URI: {}, token: {}", request.getRequestURI(), jwt);

        return jwt;
    }

    private void checkAPIKey(HttpServletRequest request) throws ServletException {
        final String requestUri = request.getRequestURI();
        final String xapiHeader = request.getHeader("X-API-KEY");

        if (requestUri != null) {
            log.trace("AuthTokenFilter - checkAPIKey - requestUri: {}", requestUri);
            if (requestUri.startsWith(timeseriesBasepath)) {
                log.trace("AuthTokenFilter - checkAPIKey - requestUri match /adres/api/v1: {}", requestUri);
                String refererHeader = request.getHeader("Referer");
                String ipAddress = getClientIpAddr(request);
                if ((xapiHeader == null) || (!apiKeyService.isValidApiKey(xapiHeader))) {
                    log.error("AuthTokenFilter - checkAPIKey - No (valid) X-API-KEY, referer: {}, ipaddress: {}", (refererHeader != null ? refererHeader : ""), ipAddress);
                    throw new ServletException("X-API-KEY has invalid format or is not known!");
                }
                log.trace("doFilterInternal - X-API-KEY, referer: {}, ipaddress: {}", (refererHeader != null ? refererHeader : ""), ipAddress);
            }
        }
    }

    private String getClientIpAddr(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

}
