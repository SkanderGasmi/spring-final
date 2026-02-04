package com.project.back_end.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.project.back_end.models.Prescription;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {
    
    // This method should already exist
    List<Prescription> findByAppointmentId(Long appointmentId);
    
    // Add these missing methods:
    
    List<Prescription> findByDoctorId(Long doctorId);
    

    List<Prescription> findByMedicationContainingIgnoreCase(String medicationName);
}