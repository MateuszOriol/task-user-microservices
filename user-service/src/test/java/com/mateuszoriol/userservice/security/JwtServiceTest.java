package com.mateuszoriol.userservice.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mateuszoriol.userservice.entity.Role;
import com.mateuszoriol.userservice.entity.User;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    @Test
    void shouldGenerateAndParseToken() {
        JwtService jwtService = new JwtService("test-secret-key", 3600000);

        User user = new User();
        user.setId(1L);
        user.setUsername("mateusz_1");
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token));
        assertEquals("mateusz_1", jwtService.extractUsername(token));
        assertEquals(1L, jwtService.extractUserId(token));
        assertEquals("USER", jwtService.extractRole(token));
    }
}