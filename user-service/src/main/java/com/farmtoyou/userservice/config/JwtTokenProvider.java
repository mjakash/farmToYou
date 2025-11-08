package com.farmtoyou.userservice.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.farmtoyou.userservice.entity.User;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey jwtSecretKey;
    private final long jwtExpirationMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret,
                            @Value("${jwt.expiration-ms}") long jwtExpirationMs) {
        byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
        this.jwtSecretKey = Keys.hmacShaKeyFor(decodedKey);
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(User user) {
        // The "principal" is the user's email in our case
        String email = user.getEmail();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .signWith(jwtSecretKey, Jwts.SIG.HS512)
                .compact();
    }
}