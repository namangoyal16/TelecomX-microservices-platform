package com.telecomx.gateway.config;

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

    public boolean isValid(String token) {
        try {
            Date expiry = Jwts.parser().verifyWith(signingKey).build()
                    .parseSignedClaims(token).getPayload().getExpiration();
            return !expiry.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
