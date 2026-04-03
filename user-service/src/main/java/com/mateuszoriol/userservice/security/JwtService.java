package com.mateuszoriol.userservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mateuszoriol.userservice.entity.User;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();

        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("userId", user.getId())
                .withClaim("role", user.getRole().name())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusMillis(expirationMs)))
                .sign(algorithm);
    }

    public String extractUsername(String token) {
        return verify(token).getSubject();
    }

    public Long extractUserId(String token) {
        return verify(token).getClaim("userId").asLong();
    }

    public String extractRole(String token) {
        return verify(token).getClaim("role").asString();
    }

    public boolean isTokenValid(String token) {
        try {
            verify(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private DecodedJWT verify(String token) {
        return JWT.require(algorithm)
                .build()
                .verify(token);
    }
}