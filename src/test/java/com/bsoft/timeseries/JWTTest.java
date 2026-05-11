package com.bsoft.timeseries;

import com.bsoft.timeseries.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JWTTest {

    private final JwtUtils jwtUtils = new JwtUtils(
            "9dd059be604f3e0b8742724eae817a49d4561fe22bf3519d10d7431dd4b188e4",
            1000000L
    );

    @Test
    @DisplayName("Generated jwt token")
    public void testGeneratedJwtToken() {
        UserDetails mockUserDetails = Mockito.mock(UserDetails.class);
        Mockito.when(mockUserDetails.getUsername()).thenReturn("admin");

        String token = jwtUtils.generateTokenFromUsername(mockUserDetails);
        log.info("Generated token: {}", token);
        assertTrue(jwtUtils.validateToken(token));
    }
}


