package com.project.back_end.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    @Autowired
    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Create a new patient.
     * 
     * @param patient the patient to create
     * @return 1 for success, 0 for failure
     */
    @Transactional
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get all appointments for a specific patient.
     * 
     * @param id the patient ID
     * @param token the JWT token
     * @return response with appointments or error
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract email from token
            String emailFromToken = tokenService.extractEmail(token);
            if (emailFromToken == null) {
                response.put("error", "Invalid token");
                return ResponseEntity.status(401).body(response);
            }
            
            // Get patient by email from token
            Patient patientFromToken = patientRepository.findByEmail(emailFromToken);
            if (patientFromToken == null) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(404).body(response);
            }
            
            // Verify patient ID matches
            if (!patientFromToken.getId().equals(id)) {
                response.put("error", "Unauthorized access to patient appointments");
                return ResponseEntity.status(403).body(response);
            }
            
            // Get appointments for patient
            List<Appointment> appointments = appointmentRepository.findByPatientId(id);
            
            // Convert to DTOs
            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(appointment -> convertToDTO(appointment))
                    .collect(Collectors.toList());
            
            response.put("appointments", appointmentDTOs);
            response.put("patientName", patientFromToken.getName());
            response.put("totalAppointments", appointmentDTOs.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Filter appointments by condition (past or future).
     * 
     * @param condition "past" or "future"
     * @param id patient ID
     * @return response with filtered appointments
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!patientRepository.existsById(id)) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(404).body(response);
            }
            
            int status;
            if ("past".equalsIgnoreCase(condition)) {
                status = 1; // Completed/Closed appointments
            } else if ("future".equalsIgnoreCase(condition)) {
                status = 0; // Scheduled/Upcoming appointments
            } else {
                response.put("error", "Invalid condition. Use 'past' or 'future'");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<Appointment> appointments = appointmentRepository
                    .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status);
            
            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(appointment -> convertToDTO(appointment))
                    .collect(Collectors.toList());
            
            response.put("appointments", appointmentDTOs);
            response.put("condition", condition);
            response.put("totalAppointments", appointmentDTOs.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Filter appointments by doctor name.
     * 
     * @param name doctor name (partial match)
     * @param patientId patient ID
     * @return response with filtered appointments
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!patientRepository.existsById(patientId)) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(404).body(response);
            }
            
            List<Appointment> appointments = appointmentRepository
                    .filterByDoctorNameAndPatientId(name, patientId);
            
            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(appointment -> convertToDTO(appointment))
                    .collect(Collectors.toList());
            
            response.put("appointments", appointmentDTOs);
            response.put("doctorName", name);
            response.put("patientId", patientId);
            response.put("totalAppointments", appointmentDTOs.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Filter appointments by doctor name and condition.
     * 
     * @param condition "past" or "future"
     * @param name doctor name (partial match)
     * @param patientId patient ID
     * @return response with filtered appointments
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, 
                                                                          String name, 
                                                                          long patientId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!patientRepository.existsById(patientId)) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(404).body(response);
            }
            
            int status;
            if ("past".equalsIgnoreCase(condition)) {
                status = 1; // Completed/Closed appointments
            } else if ("future".equalsIgnoreCase(condition)) {
                status = 0; // Scheduled/Upcoming appointments
            } else {
                response.put("error", "Invalid condition. Use 'past' or 'future'");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<Appointment> appointments = appointmentRepository
                    .filterByDoctorNameAndPatientIdAndStatus(name, patientId, status);
            
            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                    .map(appointment -> convertToDTO(appointment))
                    .collect(Collectors.toList());
            
            response.put("appointments", appointmentDTOs);
            response.put("condition", condition);
            response.put("doctorName", name);
            response.put("patientId", patientId);
            response.put("totalAppointments", appointmentDTOs.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get patient details from token.
     * 
     * @param token JWT token
     * @return response with patient details
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = tokenService.extractEmail(token);
            if (email == null) {
                response.put("error", "Invalid token");
                return ResponseEntity.status(401).body(response);
            }
            
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(404).body(response);
            }
            
            // Create a response with only necessary patient details
            Map<String, Object> patientDetails = new HashMap<>();
            patientDetails.put("id", patient.getId());
            patientDetails.put("name", patient.getName());
            patientDetails.put("email", patient.getEmail());
            patientDetails.put("phone", patient.getPhone());
            patientDetails.put("address", patient.getAddress());
            
            response.put("patient", patientDetails);
            response.put("message", "Patient details retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Convert Appointment entity to AppointmentDTO.
     * 
     * @param appointment the appointment entity
     * @return the appointment DTO
     */
    private AppointmentDTO convertToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        
        if (appointment.getDoctor() != null) {
            dto.setDoctorName(appointment.getDoctor().getName());
            dto.setDoctorId(appointment.getDoctor().getId());
        }
        
        if (appointment.getPatient() != null) {
            dto.setPatientName(appointment.getPatient().getName());
            dto.setPatientId(appointment.getPatient().getId());
        }
        
        return dto;
    }

    /**
     * Get patient by email.
     * 
     * @param email patient email
     * @return the patient or null if not found
     */
    @Transactional(readOnly = true)
    public Patient getPatientByEmail(String email) {
        return patientRepository.findByEmail(email);
    }

    /**
     * Get patient by ID.
     * 
     * @param id patient ID
     * @return the patient or null if not found
     */
    @Transactional(readOnly = true)
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id).orElse(null);
    }

    /**
     * Check if patient exists by email.
     * 
     * @param email patient email
     * @return true if exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean patientExistsByEmail(String email) {
        return patientRepository.findByEmail(email) != null;
    }

    /**
     * Update patient information.
     * 
     * @param patient updated patient data
     * @return 1 for success, 0 for failure
     */
    @Transactional
    public int updatePatient(Patient patient) {
        try {
            if (!patientRepository.existsById(patient.getId())) {
                return -1; // Patient not found
            }
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get upcoming appointments for a patient.
     * 
     * @param patientId patient ID
     * @return list of upcoming appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointments(Long patientId) {
        return appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(
                patientId, 0); // Status 0 = upcoming
    }

    /**
     * Get past appointments for a patient.
     * 
     * @param patientId patient ID
     * @return list of past appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> getPastAppointments(Long patientId) {
        return appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(
                patientId, 1); // Status 1 = past/completed
    }
}