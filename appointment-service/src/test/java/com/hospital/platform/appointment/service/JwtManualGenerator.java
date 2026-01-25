package com.hospital.platform.appointment.service;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtManualGenerator {

	public static void main(String[] args) {

		// 🔴 MUST match application.yml exactly
		String secret = "my-super-secure-jwt-secret-key-256-bit-long!!";

		Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

		String token = Jwts.builder()
				.setSubject("testuser")
				.claim("roles", List.of("ROLE_ADMIN", "ROLE_DOCTOR"))
				.setIssuedAt(new Date(1706180400L * 1000))
				.setExpiration(new Date(1893456000L * 1000))
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();

		System.out.println("JWT TOKEN:");
		System.out.println(token);
	}
}
