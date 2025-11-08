package com. project.back_end.controllers;

import com. project.back_end.models.Appointment;
import com. project.back_end.models.Doctor;
import com. project.back_end.models.Patient;
import com. project.back_end.repo.DoctorRepository;
import com. project.back_end.repo.PatientRepository;
import com. project.back_end.services.AppointmentService;
import com. project.back_end.services.Service;
import com. project.back_end.services.TokenService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

	private final AppointmentService appointmentService;
	private final Service sharedService;
	private final TokenService tokenService;
	private final DoctorRepository doctorRepository;
	private final PatientRepository patientRepository;

	@Autowired
	public AppointmentController(AppointmentService appointmentService,
			Service sharedService,
			TokenService tokenService,
			DoctorRepository doctorRepository,
			PatientRepository patientRepository) {
		this.appointmentService = appointmentService;
		this.sharedService = sharedService;
		this.tokenService = tokenService;
		this.doctorRepository = doctorRepository;
		this.patientRepository = patientRepository;
	}

	/**
	 * GET /appointments/{date}/{patientName}/{token}
	 * Returns appointments for the authenticated doctor for the given date and patient name filter.
	 */
	@GetMapping("/{date}/{patientName}/{token}")
	public ResponseEntity<?> getAppointments(@PathVariable String date,
			@PathVariable String patientName,
			@PathVariable String token) {
		String validation = sharedService.validateToken(token, "doctor");
		if (!validation.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
		}

		try {
				String identifier = tokenService.extractIdentifier(token);
				if (identifier == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
				Optional<Doctor> dOpt = doctorRepository.findByEmail(identifier) != null ? Optional.ofNullable(doctorRepository.findByEmail(identifier)) : Optional.empty();
			if (dOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Doctor not found");
			Long doctorId = dOpt.get().getId();
			LocalDate ld = LocalDate.parse(date);
			List<Appointment> appts = appointmentService.getAppointments(doctorId, ld, patientName);
			return ResponseEntity.ok(appts);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/**
	 * POST /appointments/{token}
	 * Book a new appointment as the patient identified by token.
	 */
	@PostMapping("/{token}")
	public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
		String validation = sharedService.validateToken(token, "patient");
		if (!validation.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);

		try {
				String identifier = tokenService.extractIdentifier(token);
				if (identifier == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
				Patient patient = patientRepository.findByEmail(identifier);
			if (patient == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Patient not found");

			if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Doctor id required");
			}
			Optional<Doctor> dOpt = doctorRepository.findById(appointment.getDoctor().getId());
			if (dOpt.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid doctor id");
			appointment.setDoctor(dOpt.get());
			appointment.setPatient(patient);

			int res = appointmentService.bookAppointment(appointment);
			if (res == 1) return ResponseEntity.ok(java.util.Map.of("message", "Booked"));
			return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "Could not book appointment - timeslot may be taken or invalid data"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/**
	 * PUT /appointments/{token}
	 * Update an existing appointment by patient.
	 */
	@PutMapping("/{token}")
	public ResponseEntity<?> updateAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
		String validation = sharedService.validateToken(token, "patient");
		if (!validation.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);

		try {
				String identifier = tokenService.extractIdentifier(token);
				if (identifier == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
				Patient patient = patientRepository.findByEmail(identifier);
			if (patient == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Patient not found");

			if (appointment.getId() == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Appointment id required");

			String msg = appointmentService.updateAppointment(appointment.getId(), appointment, patient.getId());
			if ("OK".equals(msg)) return ResponseEntity.ok(java.util.Map.of("message", "Updated"));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("message", msg));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/**
	 * DELETE /appointments/{id}/{token}
	 * Cancel an appointment by id (patient must own appointment).
	 */
	@DeleteMapping("/{id}/{token}")
	public ResponseEntity<?> cancelAppointment(@PathVariable Long id, @PathVariable String token) {
		String validation = sharedService.validateToken(token, "patient");
		if (!validation.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);

		try {
				String identifier = tokenService.extractIdentifier(token);
				if (identifier == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
				Patient patient = patientRepository.findByEmail(identifier);
			if (patient == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Patient not found");

			boolean ok = appointmentService.cancelAppointment(id, patient.getId());
			if (ok) return ResponseEntity.ok(java.util.Map.of("message", "Cancelled"));
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message", "Could not cancel appointment"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

}
