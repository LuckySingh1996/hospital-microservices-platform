package com.hospital.platform.patient.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	public JwtAuthenticationFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		// ✅ Allow requests without token to continue
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			String token = authHeader.substring(7);
			Claims claims = this.jwtUtil.validateToken(token);

			List<SimpleGrantedAuthority> authorities = this.jwtUtil.extractRoles(claims).stream()
					.map(r -> new SimpleGrantedAuthority("ROLE_" + r))
					.toList();

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					this.jwtUtil.extractUsername(claims),
					null,
					authorities);

			SecurityContextHolder.getContext().setAuthentication(authentication);

		} catch (Exception ex) {
			SecurityContextHolder.clearContext();
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.getWriter().write("{\"error\":\"Invalid or expired JWT\"}");
			return;
		}

		filterChain.doFilter(request, response);
	}
}
