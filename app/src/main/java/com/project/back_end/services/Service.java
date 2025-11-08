package com.project.back_end.services;

import org.springframework.beans.factory.annotation.Autowired;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.PatientService;

@org.springframework.stereotype.Service
public class Service {
	private final TokenService tokenService;
	private final AdminRepository adminRepository;
	private final DoctorRepository doctorRepository;
	private final PatientRepository patientRepository;
	private final DoctorService doctorService;
	private final PatientService patientService;

	@Autowired
	public Service(TokenService tokenService,
			    AdminRepository adminRepository,
			    DoctorRepository doctorRepository,
			    PatientRepository patientRepository,
			    DoctorService doctorService,
			    PatientService patientService) {
		this.tokenService = tokenService;
		this.adminRepository = adminRepository;
		this.doctorRepository = doctorRepository;
		this.patientRepository = patientRepository;
		this.doctorService = doctorService;
		this.patientService = patientService;
	}
// 1. **@Service Annotation**
// The @Service annotation marks this class as a service component in Spring. This allows Spring to automatically detect it through component scanning
// and manage its lifecycle, enabling it to be injected into controllers or other services using @Autowired or constructor injection.

// 2. **Constructor Injection for Dependencies**
// The constructor injects all required dependencies (TokenService, Repositories, and other Services). This approach promotes loose coupling, improves testability,
// and ensures that all required dependencies are provided at object creation time.

// 3. **validateToken Method**
// This method checks if the provided JWT token is valid for a specific user. It uses the TokenService to perform the validation.
// If the token is invalid or expired, it returns a 401 Unauthorized response with an appropriate error message. This ensures security by preventing
// unauthorized access to protected resources.

// 4. **validateAdmin Method**
// This method validates the login credentials for an admin user.
// - It first searches the admin repository using the provided username.
// - If an admin is found, it checks if the password matches.
// - If the password is correct, it generates and returns a JWT token (using the admin’s username) with a 200 OK status.
// - If the password is incorrect, it returns a 401 Unauthorized status with an error message.
// - If no admin is found, it also returns a 401 Unauthorized.
// - If any unexpected error occurs during the process, a 500 Internal Server Error response is returned.
// This method ensures that only valid admin users can access secured parts of the system.

// 5. **filterDoctor Method**
// This method provides filtering functionality for doctors based on name, specialty, and available time slots.
// - It supports various combinations of the three filters.
// - If none of the filters are provided, it returns all available doctors.
// This flexible filtering mechanism allows the frontend or consumers of the API to search and narrow down doctors based on user criteria.

// 6. **validateAppointment Method**
// This method validates if the requested appointment time for a doctor is available.
// - It first checks if the doctor exists in the repository.
// - Then, it retrieves the list of available time slots for the doctor on the specified date.
// - It compares the requested appointment time with the start times of these slots.
// - If a match is found, it returns 1 (valid appointment time).
// - If no matching time slot is found, it returns 0 (invalid).
// - If the doctor doesn’t exist, it returns -1.
// This logic prevents overlapping or invalid appointment bookings.

// 7. **validatePatient Method**
// This method checks whether a patient with the same email or phone number already exists in the system.
// - If a match is found, it returns false (indicating the patient is not valid for new registration).
// - If no match is found, it returns true.
// This helps enforce uniqueness constraints on patient records and prevent duplicate entries.

// 8. **validatePatientLogin Method**
// This method handles login validation for patient users.
// - It looks up the patient by email.
// - If found, it checks whether the provided password matches the stored one.
// - On successful validation, it generates a JWT token and returns it with a 200 OK status.
// - If the password is incorrect or the patient doesn't exist, it returns a 401 Unauthorized with a relevant error.
// - If an exception occurs, it returns a 500 Internal Server Error.
// This method ensures only legitimate patients can log in and access their data securely.

// 9. **filterPatient Method**
// This method filters a patient's appointment history based on condition and doctor name.
// - It extracts the email from the JWT token to identify the patient.
// - Depending on which filters (condition, doctor name) are provided, it delegates the filtering logic to PatientService.
// - If no filters are provided, it retrieves all appointments for the patient.
// This flexible method supports patient-specific querying and enhances user experience on the client side.


	/**
	 * Validate a token for a given role.
	 * Returns an empty string when token is valid for the role, otherwise returns an error message.
	 */
	public String validateToken(String token, String role) {
		if (token == null || token.isEmpty()) return "Invalid token";
		boolean ok = tokenService.validateToken(token, role);
		return ok ? "" : "Invalid or expired token";
	}


	/**
	 * Validate admin credentials. Returns a JWT token string on success, or null on failure.
	 */
	public String validateAdmin(String username, String password) {
		if (username == null || password == null){ 
                System.out.println("*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n");
                System.out.println("Something is null : " + username + " / " + password);
                System.out.println("*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n");
                return null; 
            }
		try {
			var admin = adminRepository.findByUsername(username);
            System.out.println("*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n");
            System.out.println("Admin found in Service: " + admin.getUsername());
            System.out.println("*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n*\n");
			if (admin == null) return null;
			if (!admin.getPassword().equals(password)) return null;
			// use username as token subject for admin
			return tokenService.generateToken(admin.getUsername());
		} catch (Exception e) {
			System.out.println("Error validating admin: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Filter doctors by name, speciality and time period. Delegates to DoctorService.
	 */
	public java.util.List<com. project.back_end.models.Doctor> filterDoctor(String name, String speciality, String time) {
		return doctorService.filterDoctorsByNameSpecialityAndTime(name, speciality, time);
	}

	/**
	 * Validate whether the requested appointment time is available for the doctor.
	 * Returns: 1 = valid, 0 = invalid timeslot, -1 = doctor not found
	 */
	public int validateAppointment(Long doctorId, java.time.LocalDateTime requestedTime) {
		if (doctorId == null || requestedTime == null) return 0;
		var dOpt = doctorRepository.findById(doctorId);
		if (dOpt.isEmpty()) return -1;
		var doctor = dOpt.get();
		var slots = doctor.getAvailableTimes();
		if (slots == null) return 0;
		java.time.LocalTime reqTime = requestedTime.toLocalTime();
		for (String slot : slots) {
			try {
				String[] parts = slot.split("-");
				java.time.LocalTime start = java.time.LocalTime.parse(parts[0].trim());
				if (start.equals(reqTime)) return 1;
			} catch (Exception e) {
				// ignore malformed slot
			}
		}
		return 0;
	}

	/**
	 * Check if patient with given email or phone already exists. Returns true if valid (no existing patient), false if duplicate.
	 */
	public boolean validatePatient(String email, String phone) {
		if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())) return false;
		var p = patientRepository.findByEmailOrPhone(email, phone);
		return p == null;
	}

	/**
	 * Validate patient login; returns JWT token on success or null on failure.
	 */
	public String validatePatientLogin(String email, String password) {
		if (email == null || password == null) return null;
		try {
			var patient = patientRepository.findByEmail(email);
			if (patient == null) return null;
			if (!patient.getPassword().equals(password)) return null;
			return tokenService.generateToken(patient.getEmail());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Filter a patient's appointment history based on condition and/or doctor name.
	 * Delegates to PatientService for actual filters. Returns empty list on error.
	 */
	public java.util.List<com. project.back_end.DTO.AppointmentDTO> filterPatient(String token, String condition, String doctorName) {
		try {
			String identifier = tokenService.extractIdentifier(token);
			if (identifier == null) return java.util.Collections.emptyList();
			var patient = patientRepository.findByEmail(identifier);
			if (patient == null) return java.util.Collections.emptyList();
			Long patientId = patient.getId();
			if ((condition == null || condition.isBlank()) && (doctorName == null || doctorName.isBlank())) {
				return patientService.getPatientAppointments(patientId);
			} else if (condition == null || condition.isBlank()) {
				return patientService.filterByDoctor(patientId, doctorName);
			} else if (doctorName == null || doctorName.isBlank()) {
				return patientService.filterByCondition(patientId, condition);
			} else {
				return patientService.filterByDoctorAndCondition(patientId, doctorName, condition);
			}
		} catch (Exception e) {
			return java.util.Collections.emptyList();
		}
	}

}
