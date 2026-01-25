package com.hospital.platform.patient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hospital.platform.patient.security.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
				// 🔴 VERY IMPORTANT
				.securityMatcher("/**")

				.csrf(CsrfConfigurer::disable)
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// 🔴 DISABLE ALL DEFAULT AUTH
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)

				.authorizeHttpRequests(auth -> auth

						// Actuator (ADMIN only)
						.requestMatchers("/actuator/**").hasRole("ADMIN")

						// Patient APIs
						.requestMatchers(HttpMethod.POST, "/api/v1/patients")
						.hasAnyRole("ADMIN", "RECEPTIONIST")

						.requestMatchers(HttpMethod.GET, "/api/v1/patients/**")
						.hasAnyRole("ADMIN", "DOCTOR")

						.requestMatchers(HttpMethod.DELETE, "/api/v1/patients/**")
						.hasRole("ADMIN")

						.anyRequest().authenticated())

				// 🔴 JWT FILTER MUST COME HERE
				.addFilterBefore(this.jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
