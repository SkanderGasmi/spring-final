package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;
    private final AuthService authService;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              TokenService tokenService,
                              AuthService authService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.authService = authService;
    }

    /**
     * Book a new appointment.
     * 
     * @param appointment the appointment object to book
     * @return 1 if successful, 0 if there's an error
     */
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Update an existing appointment.
     * 
     * @param appointment the appointment object with updated data
     * @return response entity with success or error message
     */
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();
        
        Optional<Appointment> existingAppointmentOpt = appointmentRepository.findById(appointment.getId());
        if (existingAppointmentOpt.isEmpty()) {
            response.put("error", "Appointment not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        Appointment existingAppointment = existingAppointmentOpt.get();
        
        // Check if patient ID matches
        if (!existingAppointment.getPatient().getId().equals(appointment.getPatient().getId())) {
            response.put("error", "Patient ID does not match");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Validate the appointment update
        if (authService.validateAppointment(appointment) == 0) {
            response.put("error", "Appointment validation failed");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // Update appointment details
            existingAppointment.setAppointmentTime(appointment.getAppointmentTime());
            existingAppointment.setDoctor(appointment.getDoctor());
            existingAppointment.setStatus(appointment.getStatus());
            
            appointmentRepository.save(existingAppointment);
            response.put("message", "Appointment updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to update appointment: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Cancel an existing appointment.
     * 
     * @param id the ID of the appointment to cancel
     * @param token the authorization token
     * @return response entity with success or error message
     */
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();
        
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isEmpty()) {
            response.put("error", "Appointment not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        Appointment appointment = appointmentOpt.get();
        
        // Extract patient ID from token
        Long patientIdFromToken = tokenService.extractPatientId(token);
        if (patientIdFromToken == null || 
            !appointment.getPatient().getId().equals(patientIdFromToken)) {
            response.put("error", "Unauthorized: You can only cancel your own appointments");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            appointmentRepository.delete(appointment);
            response.put("message", "Appointment cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to cancel appointment: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Retrieve appointments for a specific doctor on a specific date.
     * 
     * @param pname patient name to filter by (optional)
     * @param date the date for appointments
     * @param token the authorization token
     * @return map containing the list of appointments
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> response = new HashMap<>();
        
        // Extract doctor ID from token
        Long doctorId = tokenService.extractDoctorId(token);
        if (doctorId == null) {
            response.put("error", "Invalid token or user is not a doctor");
            return response;
        }
        
        // Verify doctor exists
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            response.put("error", "Doctor not found");
            return response;
        }
        
        // Create date range for the entire day
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        List<Appointment> appointments;
        
        if (pname != null && !pname.trim().isEmpty()) {
            // Filter by patient name
            appointments = appointmentRepository
                .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doctorId, pname, startOfDay, endOfDay);
        } else {
            // Get all appointments for the day
            appointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);
        }
        
        response.put("appointments", appointments);
        response.put("doctorName", doctorOpt.get().getName());
        response.put("date", date.toString());
        response.put("totalAppointments", appointments.size());
        
        return response;
    }

    /**
     * Change the status of an appointment.
     * 
     * @param id the ID of the appointment
     * @param status the new status
     * @return response entity with success or error message
     */
    @Transactional
    public ResponseEntity<Map<String, String>> changeStatus(long id, int status) {
        Map<String, String> response = new HashMap<>();
        
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isEmpty()) {
            response.put("error", "Appointment not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            appointmentRepository.updateStatus(status, id);
            response.put("message", "Appointment status updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to update appointment status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get all appointments for a specific patient.
     * 
     * @param patientId the ID of the patient
     * @return list of appointments for the patient
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    /**
     * Get appointments for a patient filtered by status.
     * 
     * @param patientId the ID of the patient
     * @param status the status to filter by
     * @return list of filtered appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByPatientIdAndStatus(Long patientId, int status) {
        return appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status);
    }
}