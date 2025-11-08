package com. project.back_end.controllers;

import com. project.back_end.DTO.Login;
import com. project.back_end.models.Doctor;
import com. project.back_end.services.DoctorService;
import com. project.back_end.services.Service;
import com. project.back_end.services.TokenService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

	private final DoctorService doctorService;
	private final Service sharedService;
	private final TokenService tokenService;

	@Autowired
	public DoctorController(DoctorService doctorService, Service sharedService, TokenService tokenService) {
		this.doctorService = doctorService;
		this.sharedService = sharedService;
		this.tokenService = tokenService;
	}
	/**
	 * GET /doctor/availability/{user}/{doctorId}/{date}/{token}
	 * Returns available time slots for a doctor on a date. `user` is expected to be the role ("doctor" or "patient" or "admin")
	 */
	@GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
	public ResponseEntity<?> getDoctorAvailability(@PathVariable String user,
												   @PathVariable Long doctorId,
												   @PathVariable String date,
												   @PathVariable String token) {
		String validation = sharedService.validateToken(token, user);
		if (!validation.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
		}
		try {
			LocalDate ld = LocalDate.parse(date);
			List<String> slots = doctorService.getDoctorAvailability(doctorId, ld);
			return ResponseEntity.ok(Map.of("available", slots));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	/**
	 * GET /doctor/ - return all doctors
	 */
	@GetMapping({"","/"})
	public ResponseEntity<?> getDoctor() {
		try {
			List<Doctor> doctors = doctorService.getDoctors();
			return ResponseEntity.ok(Map.of("doctors", doctors));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/**
	 * POST /doctor/{token} - create a new doctor (admin only)
	 */
	@PostMapping("/{token}")
	public ResponseEntity<?> saveDoctor(@Validated @RequestBody Doctor doctor, @PathVariable String token) {
		String validation = sharedService.validateToken(token, "admin");
		if (!validation.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
		try {
			int res = doctorService.saveDoctor(doctor);
			if (res == -1) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Doctor with email already exists"));
			if (res == 1) return ResponseEntity.ok(Map.of("message", "Saved"));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Could not save doctor"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/**
	 * POST /doctor/login - doctor login
	 */
	@PostMapping("/login")
	public ResponseEntity<?> doctorLogin(@Validated @RequestBody Login login) {
		try {
			String token = doctorService.validateDoctor(login);
			if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
			return ResponseEntity.ok(Map.of("token", token));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/**
	 * PUT /doctor/{token} - update doctor (admin only)
	 */
	@PutMapping("/{token}")
	public ResponseEntity<?> updateDoctor(@Validated @RequestBody Doctor doctor, @PathVariable String token) {
		String validation = sharedService.validateToken(token, "admin");
		if (!validation.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
		try {
			int res = doctorService.updateDoctor(doctor);
			if (res == -1) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found"));
			if (res == 1) return ResponseEntity.ok(Map.of("message", "Updated"));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Could not update doctor"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/**
	 * DELETE /doctor/{id}/{token} - delete doctor (admin only)
	 */
	@DeleteMapping("/{id}/{token}")
	public ResponseEntity<?> deleteDoctor(@PathVariable Long id, @PathVariable String token) {
		String validation = sharedService.validateToken(token, "admin");
		if (!validation.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
		try {
			int res = doctorService.deleteDoctor(id);
			if (res == -1) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found"));
			if (res == 1) return ResponseEntity.ok(Map.of("message", "Deleted"));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Could not delete doctor"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/**
	 * GET /doctor/filter/{name}/{time}/{speciality}
	 * Filters doctors by name, time (AM/PM) and speciality. Use 'null' for unused params.
	 */
	@GetMapping("/filter/{name}/{time}/{speciality}")
	public ResponseEntity<?> filter(@PathVariable String name, @PathVariable String time, @PathVariable String speciality) {
		try {
			String n = (name == null || "null".equalsIgnoreCase(name) || "".equals(name)) ? null : name;
			String t = (time == null || "null".equalsIgnoreCase(time) || "".equals(time)) ? null : time;
			String s = (speciality == null || "null".equalsIgnoreCase(speciality) || "".equals(speciality)) ? null : speciality;
			List<Doctor> doctors = sharedService.filterDoctor(n, s, t);
			return ResponseEntity.ok(Map.of("doctors", doctors));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

}
