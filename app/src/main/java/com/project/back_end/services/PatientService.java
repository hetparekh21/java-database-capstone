package com. project.back_end.services;

import com. project.back_end.DTO.AppointmentDTO;
import com. project.back_end.models.Appointment;
import com. project.back_end.models.Patient;
import com. project.back_end.repo.AppointmentRepository;
import com. project.back_end.repo.PatientRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    @Autowired
    public PatientService(PatientRepository patientRepository, AppointmentRepository appointmentRepository, TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Create a new patient. Returns 1 on success, -1 if patient already exists (email or phone), 0 on error.
     */
    @Transactional
    public int createPatient(Patient patient) {
        try {
            if (patient == null) return 0;
            Patient existing = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
            if (existing != null) return -1;
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            System.err.println("Error creating patient: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get all appointments for a patient and convert to AppointmentDTO list.
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getPatientAppointments(Long patientId) {
        List<AppointmentDTO> out = new ArrayList<>();
        try {
            List<Appointment> appts = appointmentRepository.findByPatientId(patientId);
            for (Appointment a : appts) {
                out.add(mapToDTO(a));
            }
        } catch (Exception e) {
            System.err.println("Error fetching appointments for patient " + patientId + ": " + e.getMessage());
        }
        return out;
    }

    /**
     * Filter by condition ("past" -> status=1, "future" -> status=0)
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> filterByCondition(Long patientId, String condition) {
        List<AppointmentDTO> out = new ArrayList<>();
        try {
            int status;
            if ("past".equalsIgnoreCase(condition)) status = 1;
            else if ("future".equalsIgnoreCase(condition)) status = 0;
            else return out; // invalid condition -> empty
            List<Appointment> appts = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status);
            for (Appointment a : appts) out.add(mapToDTO(a));
        } catch (Exception e) {
            System.err.println("Error filtering by condition for patient " + patientId + ": " + e.getMessage());
        }
        return out;
    }

    /**
     * Filter by doctor's name for a patient.
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> filterByDoctor(Long patientId, String doctorName) {
        List<AppointmentDTO> out = new ArrayList<>();
        try {
            List<Appointment> appts = appointmentRepository.filterByDoctorNameAndPatientId(doctorName, patientId);
            for (Appointment a : appts) out.add(mapToDTO(a));
        } catch (Exception e) {
            System.err.println("Error filtering by doctor for patient " + patientId + ": " + e.getMessage());
        }
        return out;
    }

    /**
     * Filter by doctor name and condition.
     */
    @Transactional(readOnly = true)
    public List<AppointmentDTO> filterByDoctorAndCondition(Long patientId, String doctorName, String condition) {
        List<AppointmentDTO> out = new ArrayList<>();
        try {
            int status;
            if ("past".equalsIgnoreCase(condition)) status = 1;
            else if ("future".equalsIgnoreCase(condition)) status = 0;
            else return out;
            List<Appointment> appts = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, status);
            for (Appointment a : appts) out.add(mapToDTO(a));
        } catch (Exception e) {
            System.err.println("Error filtering by doctor and condition for patient " + patientId + ": " + e.getMessage());
        }
        return out;
    }

    /**
     * Get patient details from token.
     */
    @Transactional(readOnly = true)
    public Patient getPatientDetails(String token) {
        try {
            String identifier = tokenService.extractIdentifier(token);
            if (identifier == null) return null;
            return patientRepository.findByEmail(identifier);
        } catch (Exception e) {
            System.err.println("Error extracting patient details from token: " + e.getMessage());
            return null;
        }
    }

    private AppointmentDTO mapToDTO(Appointment a) {
        Patient p = a.getPatient();
        return new AppointmentDTO(
                a.getId(),
                a.getDoctor() != null ? a.getDoctor().getId() : null,
                a.getDoctor() != null ? a.getDoctor().getName() : null,
                p != null ? p.getId() : null,
                p != null ? p.getName() : null,
                p != null ? p.getEmail() : null,
                p != null ? p.getPhone() : null,
                p != null ? p.getAddress() : null,
                a.getAppointmentTime(),
                a.getStatus()
        );
    }


}
