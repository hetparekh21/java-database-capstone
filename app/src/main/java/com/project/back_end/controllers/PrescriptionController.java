package com. project.back_end.controllers;

import com. project.back_end.models.Prescription;
import com. project.back_end.models.Appointment;
import com. project.back_end.models.Doctor;
import com. project.back_end.repo.AppointmentRepository;
import com. project.back_end.repo.DoctorRepository;
import com. project.back_end.services.PrescriptionService;
import com. project.back_end.services.AppointmentService;
import com. project.back_end.services.Service;
import com. project.back_end.services.TokenService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

	private final PrescriptionService prescriptionService;
	private final Service sharedService;
	private final AppointmentService appointmentService;
	private final AppointmentRepository appointmentRepository;
	private final DoctorRepository doctorRepository;
	private final TokenService tokenService;

	@Autowired
	public PrescriptionController(PrescriptionService prescriptionService,
								  Service sharedService,
								  AppointmentService appointmentService,
								  AppointmentRepository appointmentRepository,
								  DoctorRepository doctorRepository,
								  TokenService tokenService) {
		this.prescriptionService = prescriptionService;
		this.sharedService = sharedService;
		this.appointmentService = appointmentService;
		this.appointmentRepository = appointmentRepository;
		this.doctorRepository = doctorRepository;
		this.tokenService = tokenService;
	}

	/**
	 * POST /prescription/{token}
	 * Save a prescription (doctor only). Also mark the appointment as completed (status=1) when saved.
	 */
	@PostMapping("/{token}")
	public ResponseEntity<?> savePrescription(@Validated @RequestBody Prescription prescription, @PathVariable String token) {
		String validation = sharedService.validateToken(token, "doctor");
		if (!validation.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);

		if (prescription == null || prescription.getAppointmentId() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "appointmentId required"));
		}

		try {
			// Ensure the authenticated doctor owns the appointment
			String identifier = tokenService.extractIdentifier(token);
			if (identifier == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");

			Optional<Appointment> aOpt = appointmentRepository.findById(prescription.getAppointmentId());
			if (aOpt.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Appointment not found"));
			Appointment appt = aOpt.get();

			Doctor doc = appt.getDoctor();
			if (doc == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Appointment has no doctor"));

			// verify doctor email matches token subject
			var authDoctor = doctorRepository.findByEmail(identifier);
			if (authDoctor == null || !authDoctor.getId().equals(doc.getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Not authorized for this appointment"));
			}

			int res = prescriptionService.savePrescription(prescription);
			if (res == -1) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Prescription already exists for appointment"));
			if (res == 1) {
				// mark appointment as completed (status = 1)
				appointmentService.changeStatus(prescription.getAppointmentId(), 1);
				return ResponseEntity.ok(Map.of("message", "Saved"));
			}
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Could not save prescription"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/**
	 * GET /prescription/{appointmentId}/{token}
	 * Get prescriptions for an appointment (doctor only).
	 */
	@GetMapping("/{appointmentId}/{token}")
	public ResponseEntity<?> getPrescription(@PathVariable Long appointmentId, @PathVariable String token) {
		String validation = sharedService.validateToken(token, "doctor");
		if (!validation.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);

		try {
			String identifier = tokenService.extractIdentifier(token);
			if (identifier == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");

			Optional<Appointment> aOpt = appointmentRepository.findById(appointmentId);
			if (aOpt.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Appointment not found"));
			Appointment appt = aOpt.get();

			// ensure doctor owns the appointment
			Doctor doc = appt.getDoctor();
			var authDoctor = doctorRepository.findByEmail(identifier);
			if (authDoctor == null || doc == null || !authDoctor.getId().equals(doc.getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Not authorized"));
			}

			List<com. project.back_end.models.Prescription> list = prescriptionService.getPrescription(appointmentId);
			return ResponseEntity.ok(Map.of("prescriptions", list));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

}
