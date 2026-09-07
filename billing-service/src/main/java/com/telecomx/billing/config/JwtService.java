package com.telecomx.billing.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtService {
    private final SecretKey signingKey;
    public JwtService(@Value("${jwt.secret}") String base64Secret) {
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
    }
    public boolean isTokenValid(String token) {
        try { return !extractAllClaims(token).getExpiration().before(new Date()); }
        catch (Exception e) { return false; }
    }
    public String extractEmail(String token) { return extractAllClaims(token).getSubject(); }
    public String extractRole(String token) { return extractAllClaims(token).get("role", String.class); }
    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}
