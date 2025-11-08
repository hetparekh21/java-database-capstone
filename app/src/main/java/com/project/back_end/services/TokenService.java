package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenService {

	private final AdminRepository adminRepository;
	private final DoctorRepository doctorRepository;
	private final PatientRepository patientRepository;

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Autowired
	public TokenService(AdminRepository adminRepository, DoctorRepository doctorRepository, PatientRepository patientRepository) {
		this.adminRepository = adminRepository;
		this.doctorRepository = doctorRepository;
		this.patientRepository = patientRepository;
	}

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}

	public String generateToken(String email) {
		long now = System.currentTimeMillis();
		return Jwts.builder()
				.setSubject(email)
				.setIssuedAt(new Date(now))
				.setExpiration(new Date(now + 7L * 24 * 60 * 60 * 1000)) // 7 days
				.signWith(getSigningKey())
				.compact();
	}

	public String extractIdentifier(String token) {
		return Jwts.parser()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}

	public boolean validateToken(String token, String role) {
		try {
			String identifier = extractIdentifier(token);
			switch (role.toLowerCase()) {
				case "admin":
					// admin tokens carry username as subject
					return adminRepository.findByUsername(identifier) != null;
				case "doctor":
					return doctorRepository.findByEmail(identifier) != null;
				case "patient":
					return patientRepository.findByEmail(identifier) != null;
				default:
					return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

}
