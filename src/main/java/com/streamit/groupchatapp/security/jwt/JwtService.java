package com.streamit.groupchatapp.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final Key signingKey;
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    // -------------------------------
    // TOKEN GENERATION
    // -------------------------------

    public String generateToken(
            String email,
            String role,
            Long userId,
            String name
            // hello, world!
    ) {
        return Jwts.builder()
                .setSubject(email)
                .addClaims(Map.of(
                        "role", role,
                        "userId", userId,
                        "name",name
                ))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // -------------------------------
    // TOKEN VALIDATION
    // -------------------------------

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // -------------------------------
    // CLAIM EXTRACTION
    // -------------------------------

    public String extractEmail(String token) {
        return parseToken(token).getBody().getSubject();
    }

    public String extractRole(String token) {
        return parseToken(token).getBody().get("role", String.class);
    }

    public Long extractUserId(String token) {
        return parseToken(token).getBody().get("userId", Long.class);
    }
    public String extractName(String token){
        return parseToken(token).getBody().get("name", String.class);
    }

    // -------------------------------
    // INTERNAL PARSER
    // -------------------------------

    private Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
    }
    @PostConstruct
    public void check() {
        System.out.println("JWT secret loaded");
    }
}