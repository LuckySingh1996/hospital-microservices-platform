/*package com.hospital.platform.patient.security;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StartupJwtPrinter implements CommandLineRunner {

	private final JwtUtil jwtUtil;

	public StartupJwtPrinter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	public void run(String... args) {
		log.info("\n================ JWT TOKENS (DEMO ONLY) ================\n");

		log.info("ADMIN TOKEN:");
		log.info(this.jwtUtil.generateToken("admin1", List.of("ADMIN")));
		log.info("");

		log.info("DOCTOR TOKEN:");
		log.info(this.jwtUtil.generateToken("doctor1", List.of("DOCTOR")));
		log.info("");

		log.info("RECEPTIONIST TOKEN:");
		log.info(this.jwtUtil.generateToken("reception1", List.of("RECEPTIONIST")));
		log.info("");

		log.info("========================================================\n");
	}
}
*/