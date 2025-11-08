package com. project.back_end.repo;

import com. project.back_end.models.Prescription;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

	// Find prescriptions by associated appointment ID
	List<Prescription> findByAppointmentId(Long appointmentId);

}
//      - Parameters: Long appointmentId

