package com.project.back_end.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    @Autowired
    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * Save a prescription to the database.
     * 
     * @param prescription the prescription to save
     * @return response with success or error message
     */
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Check if prescription already exists for this appointment
            if (prescription.getAppointmentId() != null) {
                List<Prescription> existingPrescriptions = prescriptionRepository
                        .findByAppointmentId(prescription.getAppointmentId());
                if (!existingPrescriptions.isEmpty()) {
                    response.put("error", "Prescription already exists for this appointment");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // Save the prescription
            Prescription savedPrescription = prescriptionRepository.save(prescription);
            
            response.put("message", "Prescription saved successfully");
            response.put("prescriptionId", savedPrescription.getId());
            response.put("appointmentId", savedPrescription.getAppointmentId().toString());
            
            return ResponseEntity.status(201).body(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to save prescription: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Retrieve prescription by appointment ID.
     * 
     * @param appointmentId the appointment ID
     * @return response with prescription or error message
     */
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Prescription> prescriptions = prescriptionRepository
                    .findByAppointmentId(appointmentId);
            
            if (prescriptions.isEmpty()) {
                response.put("message", "No prescription found for this appointment");
                response.put("prescriptions", List.of());
                return ResponseEntity.ok(response);
            }
            
            response.put("prescriptions", prescriptions);
            response.put("count", prescriptions.size());
            response.put("appointmentId", appointmentId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to retrieve prescription: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get prescription by ID.
     * 
     * @param id the prescription ID
     * @return response with prescription or error message
     */
    public ResponseEntity<Map<String, Object>> getPrescriptionById(String id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            return prescriptionRepository.findById(id)
                    .map(prescription -> {
                        response.put("prescription", prescription);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        response.put("error", "Prescription not found with ID: " + id);
                        return ResponseEntity.status(404).body(response);
                    });
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to retrieve prescription: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get prescriptions by doctor ID.
     * 
     * @param doctorId the doctor ID
     * @return response with prescriptions or error message
     */
    public ResponseEntity<Map<String, Object>> getPrescriptionsByDoctorId(Long doctorId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Prescription> prescriptions = prescriptionRepository
                    .findByDoctorId(doctorId);
            
            response.put("prescriptions", prescriptions);
            response.put("count", prescriptions.size());
            response.put("doctorId", doctorId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to retrieve prescriptions: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Update an existing prescription.
     * 
     * @param id the prescription ID
     * @param prescription the updated prescription data
     * @return response with success or error message
     */
    public ResponseEntity<Map<String, String>> updatePrescription(String id, Prescription prescription) {
        Map<String, String> response = new HashMap<>();
        
        try {
            if (!prescriptionRepository.existsById(id)) {
                response.put("error", "Prescription not found with ID: " + id);
                return ResponseEntity.status(404).body(response);
            }
            
            // Ensure the ID is set
            prescription.setId(id);
            prescriptionRepository.save(prescription);
            
            response.put("message", "Prescription updated successfully");
            response.put("prescriptionId", id);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to update prescription: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Delete a prescription by ID.
     * 
     * @param id the prescription ID
     * @return response with success or error message
     */
    public ResponseEntity<Map<String, String>> deletePrescription(String id) {
        Map<String, String> response = new HashMap<>();
        
        try {
            if (!prescriptionRepository.existsById(id)) {
                response.put("error", "Prescription not found with ID: " + id);
                return ResponseEntity.status(404).body(response);
            }
            
            prescriptionRepository.deleteById(id);
            
            response.put("message", "Prescription deleted successfully");
            response.put("prescriptionId", id);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to delete prescription: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}