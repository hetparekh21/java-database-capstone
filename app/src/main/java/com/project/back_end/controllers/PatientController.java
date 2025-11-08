package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.path}patient")
public class PatientController {

	private final PatientService patientService;
	private final Service sharedService;

	@Autowired
	public PatientController(PatientService patientService, Service sharedService) {
		this.patientService = patientService;
		this.sharedService = sharedService;
	}

	/**
	 * GET /patient/{token} - return patient details inferred from token
	 */
	@GetMapping("/{token}")
	public ResponseEntity<?> getPatient(@PathVariable String token) {
		String validation = sharedService.validateToken(token, "patient");
		if (!validation.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", validation));
		try {
			Patient p = patientService.getPatientDetails(token);
			if (p == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Patient not found"));
			return ResponseEntity.ok(Map.of("patient", p));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
		}
	}

	/**
	 * POST /patient - create a new patient (signup)
	 */
	@PostMapping({"","/"})
	public ResponseEntity<?> createPatient(@Validated @RequestBody Patient patient) {
		try {
			if (!sharedService.validatePatient(patient.getEmail(), patient.getPhone())) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Patient with given email or phone already exists"));
			}
			int res = patientService.createPatient(patient);
			if (res == -1) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Patient already exists"));
			if (res == 1) return ResponseEntity.ok(Map.of("message", "Saved"));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Could not save patient"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
		}
	}

	/**
	 * POST /patient/login - patient login
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@Validated @RequestBody Login login) {
		try {
			String token = sharedService.validatePatientLogin(login.getEmail(), login.getPassword());
			if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
			return ResponseEntity.ok(Map.of("token", token));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
		}
	}

	/**
	 * GET /patient/{id}/{user}/{token} - get appointments for patient (user may be patient or doctor)
	 */
	@GetMapping("/{id}/{user}/{token}")
	public ResponseEntity<?> getPatientAppointment(@PathVariable Long id, @PathVariable String user, @PathVariable String token) {
		String validation = sharedService.validateToken(token, user);
		if (!validation.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", validation));
		try {
			var appts = patientService.getPatientAppointments(id);
			return ResponseEntity.ok(Map.of("appointments", appts));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
		}
	}

	/**
	 * GET /patient/filter/{condition}/{name}/{token} - filter patient's appointments
	 */
	@GetMapping("/filter/{condition}/{name}/{token}")
	public ResponseEntity<?> filterPatientAppointment(@PathVariable String condition, @PathVariable String name, @PathVariable String token) {
		String validation = sharedService.validateToken(token, "patient");
		if (!validation.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", validation));
		try {
			var list = sharedService.filterPatient(token, condition.equals("null") ? null : condition, (name.equals("null") ? null : name));
			return ResponseEntity.ok(Map.of("appointments", list));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
		}
	}

}


