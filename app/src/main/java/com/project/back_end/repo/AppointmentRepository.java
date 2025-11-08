package com. project.back_end.repo;

import com. project.back_end.models.Appointment;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

   // Find appointments for a doctor within a time range
   List<Appointment> findByDoctorIdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

   // Find appointments for a doctor filtered by patient name (case-insensitive) within a time range
   List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(Long doctorId, String patientName, LocalDateTime start, LocalDateTime end);

   // Delete all appointments for a doctor
   @Modifying
   @Transactional
   void deleteAllByDoctorId(Long doctorId);

   // Find all appointments for a patient
   List<Appointment> findByPatientId(Long patientId);

   // Find appointments for a patient with a given status ordered by appointment time ascending
   List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

   // Filter by doctor name (LIKE) and patient id
   @Query("SELECT a FROM Appointment a JOIN a.doctor d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) AND a.patient.id = :patientId")
   List<Appointment> filterByDoctorNameAndPatientId(@Param("doctorName") String doctorName, @Param("patientId") Long patientId);

   // Filter by doctor name, patient id and status
   @Query("SELECT a FROM Appointment a JOIN a.doctor d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) AND a.patient.id = :patientId AND a.status = :status")
   List<Appointment> filterByDoctorNameAndPatientIdAndStatus(@Param("doctorName") String doctorName, @Param("patientId") Long patientId, @Param("status") int status);

   // Update status for an appointment
   @Modifying
   @Transactional
   @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
   void updateStatus(@Param("status") int status, @Param("id") Long id);

}
