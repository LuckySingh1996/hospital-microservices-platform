package com.hospital.platform.patient.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

	@Value("${security.jwt.secret}")
	private String secret;

	@Value("${security.jwt.access-token-validity}")
	private int accessTokenValidity;

	private Key key;

	@PostConstruct
	public void init() {
		this.key = Keys.hmacShaKeyFor(this.secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(String username, List<String> roles) {
		return Jwts.builder()
				.setSubject(username)
				.claim("roles", roles)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(Date.from(Instant.now().plusSeconds(this.accessTokenValidity)))
				.signWith(this.key)
				.compact();
	}

	public Claims validateToken(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(this.key)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	public List<String> extractRoles(Claims claims) {
		return claims.get("roles", List.class);
	}

	public String extractUsername(Claims claims) {
		return claims.getSubject();
	}
}
