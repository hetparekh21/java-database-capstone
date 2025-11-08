package com. project.back_end.services;

import com. project.back_end.models.Prescription;
import com. project.back_end.repo.PrescriptionRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrescriptionService {

	private final PrescriptionRepository prescriptionRepository;

	@Autowired
	public PrescriptionService(PrescriptionRepository prescriptionRepository) {
		this.prescriptionRepository = prescriptionRepository;
	}

	/**
	 * Save a prescription. Returns:
	 *  -1 : prescription already exists for appointment
	 *   1 : saved successfully
	 *   0 : error
	 */
	@Transactional
	public int savePrescription(Prescription prescription) {
		if (prescription == null || prescription.getAppointmentId() == null) return 0;
		try {
			List<Prescription> existing = prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());
			if (existing != null && !existing.isEmpty()) {
				return -1; // already exists
			}
			prescriptionRepository.save(prescription);
			return 1;
		} catch (Exception e) {
			System.err.println("Error saving prescription: " + e.getMessage());
			return 0;
		}
	}

	/**
	 * Get prescriptions by appointmentId. Returns empty list on error or when none found.
	 */
	@Transactional(readOnly = true)
	public List<Prescription> getPrescription(Long appointmentId) {
		if (appointmentId == null) return Collections.emptyList();
		try {
			List<Prescription> found = prescriptionRepository.findByAppointmentId(appointmentId);
			return found != null ? found : Collections.emptyList();
		} catch (Exception e) {
			System.err.println("Error fetching prescription for appointment " + appointmentId + ": " + e.getMessage());
			return Collections.emptyList();
		}
	}

}
