package com.project.back_end.DTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AppointmentDTO {
	private Long id;
	private Long doctorId;
	private String doctorName;
	private Long patientId;
	private String patientName;
	private String patientEmail;
	private String patientPhone;
	private String patientAddress;
	private LocalDateTime appointmentTime;
	private int status;

	// Derived fields
	private LocalDate appointmentDate;
	private LocalTime appointmentTimeOnly;
	private LocalDateTime endTime;

	/**
	 * Constructor accepts core fields and computes derived date/time fields.
	 */
	public AppointmentDTO(Long id,
						  Long doctorId,
						  String doctorName,
						  Long patientId,
						  String patientName,
						  String patientEmail,
						  String patientPhone,
						  String patientAddress,
						  LocalDateTime appointmentTime,
						  int status) {
		this.id = id;
		this.doctorId = doctorId;
		this.doctorName = doctorName;
		this.patientId = patientId;
		this.patientName = patientName;
		this.patientEmail = patientEmail;
		this.patientPhone = patientPhone;
		this.patientAddress = patientAddress;
		this.appointmentTime = appointmentTime;
		this.status = status;

		if (this.appointmentTime != null) {
			this.appointmentDate = this.appointmentTime.toLocalDate();
			this.appointmentTimeOnly = this.appointmentTime.toLocalTime();
			this.endTime = this.appointmentTime.plusHours(1);
		}
	}

	// Getters
	public Long getId() {
		return id;
	}

	public Long getDoctorId() {
		return doctorId;
	}

	public String getDoctorName() {
		return doctorName;
	}

	public Long getPatientId() {
		return patientId;
	}

	public String getPatientName() {
		return patientName;
	}

	public String getPatientEmail() {
		return patientEmail;
	}

	public String getPatientPhone() {
		return patientPhone;
	}

	public String getPatientAddress() {
		return patientAddress;
	}

	public LocalDateTime getAppointmentTime() {
		return appointmentTime;
	}

	public int getStatus() {
		return status;
	}

	public LocalDate getAppointmentDate() {
		return appointmentDate;
	}

	public LocalTime getAppointmentTimeOnly() {
		return appointmentTimeOnly;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

}
