package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
public class AppointmentService {

	private final AppointmentRepository appointmentRepository;
	private final Service sharedService;
	private final TokenService tokenService;
	private final PatientRepository patientRepository;
	private final DoctorRepository doctorRepository;

	@Autowired
	public AppointmentService(AppointmentRepository appointmentRepository,
							  Service sharedService,
							  TokenService tokenService,
							  PatientRepository patientRepository,
							  DoctorRepository doctorRepository) {
		this.appointmentRepository = appointmentRepository;
		this.sharedService = sharedService;
		this.tokenService = tokenService;
		this.patientRepository = patientRepository;
		this.doctorRepository = doctorRepository;
	}

	/**
	 * Book a new appointment.
	 * Returns 1 on success, 0 on failure (conflict or invalid doctor/patient).
	 */
	@Transactional
	public int bookAppointment(Appointment appointment) {
		if (appointment == null || appointment.getDoctor() == null || appointment.getPatient() == null || appointment.getAppointmentTime() == null) {
			return 0;
		}

		Long doctorId = appointment.getDoctor().getId();
		if (doctorId == null || doctorRepository.findById(doctorId).isEmpty()) {
			return 0; // invalid doctor
		}

		// check for conflicting appointments for the doctor in the requested timeslot
		LocalDateTime start = appointment.getAppointmentTime();
		LocalDateTime end = start.plusHours(1);
		List<Appointment> conflicts = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);
		if (conflicts != null && !conflicts.isEmpty()) {
			return 0; // timeslot taken
		}

		try {
			appointmentRepository.save(appointment);
			return 1;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Update an existing appointment. Returns an informational message.
	 */
	@Transactional
	public String updateAppointment(Long appointmentId, Appointment updatedAppointment, Long patientId) {
		Optional<Appointment> existingOpt = appointmentRepository.findById(appointmentId);
		if (existingOpt.isEmpty()) return "Appointment not found";

		Appointment existing = existingOpt.get();
		if (!existing.getPatient().getId().equals(patientId)) return "Unauthorized: patient mismatch";

		// Check doctor exists
		Long doctorId = updatedAppointment.getDoctor() != null ? updatedAppointment.getDoctor().getId() : existing.getDoctor().getId();
		if (doctorId == null || doctorRepository.findById(doctorId).isEmpty()) {
			return "Invalid doctor";
		}

		LocalDateTime newStart = updatedAppointment.getAppointmentTime() != null ? updatedAppointment.getAppointmentTime() : existing.getAppointmentTime();
		LocalDateTime newEnd = newStart.plusHours(1);
		List<Appointment> conflicts = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, newStart, newEnd);
		// allow if only conflict is the same appointment
		boolean hasConflictingOther = conflicts.stream().anyMatch(a -> !a.getId().equals(appointmentId));
		if (hasConflictingOther) return "Requested timeslot not available";

		// apply updates
		existing.setDoctor(updatedAppointment.getDoctor() != null ? updatedAppointment.getDoctor() : existing.getDoctor());
		existing.setAppointmentTime(updatedAppointment.getAppointmentTime() != null ? updatedAppointment.getAppointmentTime() : existing.getAppointmentTime());
		existing.setPatient(updatedAppointment.getPatient() != null ? updatedAppointment.getPatient() : existing.getPatient());
		existing.setStatus(updatedAppointment.getStatus());

		appointmentRepository.save(existing);
		return "OK";
	}

	/**
	 * Cancel an appointment. Only the owning patient may cancel. Returns true on success.
	 */
	@Transactional
	public boolean cancelAppointment(Long appointmentId, Long patientId) {
		Optional<Appointment> existingOpt = appointmentRepository.findById(appointmentId);
		if (existingOpt.isEmpty()) return false;
		Appointment existing = existingOpt.get();
		if (!existing.getPatient().getId().equals(patientId)) return false;

		appointmentRepository.deleteById(appointmentId);
		return true;
	}

	/**
	 * Get appointments for a doctor on a given date, optionally filtered by patient name.
	 */
	public List<Appointment> getAppointments(Long doctorId, LocalDate date, String patientName) {
		LocalDateTime startOfDay = date.atStartOfDay();
		LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
		if (patientName == null || patientName.isBlank()) {
			return appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);
		}
		return appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(doctorId, patientName, startOfDay, endOfDay);
	}

	/**
	 * Change status of an appointment.
	 */
	@Transactional
	public boolean changeStatus(Long appointmentId, int status) {
		try {
			appointmentRepository.updateStatus(status, appointmentId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
