package com.streamit.groupchatapp.security.jwt;

import com.streamit.groupchatapp.security.principal.UserPrincipal;
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

    public String generateToken(UserPrincipal principal) {
        Map<String, Object> claims = Map.of(
                "userId", principal.id(),
                "name", principal.name(),
                "profileImageUrl", principal.profileImageUrl()
        );

        return Jwts.builder()
                .setSubject(principal.email()) // Use email as subject
                .addClaims(claims)
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


    public Long extractUserId(String token) {
        return parseToken(token).getBody().get("userId", Long.class);
    }
    public String extractName(String token){
        return parseToken(token).getBody().get("name", String.class);
    }
    public String extractProfileImageUrl(String token){
        return parseToken(token).getBody().get("profileImageUrl", String.class);
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