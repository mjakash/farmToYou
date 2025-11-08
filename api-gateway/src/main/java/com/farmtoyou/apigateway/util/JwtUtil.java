package com.farmtoyou.apigateway.util;

import java.util.Base64;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	private final SecretKey jwtSecretKey;

	public JwtUtil(@Value("${jwt.secret}") String jwtSecret) {
		byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
		this.jwtSecretKey = Keys.hmacShaKeyFor(decodedKey);

	}

	public void validateToken(final String token) {
		this.getAllClaimsFromToken(token);
	}

	public String getSubjectFromToken(String token) {
		return getAllClaimsFromToken(token).getSubject();
	}

	public Claims getAllClaimsFromToken(String token) {
		return Jwts
				.parser()
				.verifyWith(jwtSecretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public String getUserId(String token) {
		// We get the claim as an Integer and convert it to String
		return String.valueOf(getAllClaimsFromToken(token).get("userId", Integer.class));
	}

	public String getRole(String token) {
		return getAllClaimsFromToken(token).get("role", String.class);
	}
}
