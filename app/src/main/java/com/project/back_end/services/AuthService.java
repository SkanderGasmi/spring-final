package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class AuthService {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    @Autowired
    public AuthService(TokenService tokenService,
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

    /**
     * Validate a token for a specific user.
     * 
     * @param token the JWT token to validate
     * @param user the user type (DOCTOR, PATIENT, ADMIN)
     * @return response entity with error message if invalid
     */
    public Map<String, String> validateToken(String token, String user) {
    Map<String, String> response = new HashMap<>();
    
    try {
        boolean isValid = tokenService.validateToken(token, user);
        if (!isValid) {
            response.put("error", "Invalid or expired token");
            return response;
        }
        
        response.put("message", "Token is valid");
        return response;
        
    } catch (Exception e) {
        e.printStackTrace();
        response.put("error", "Error validating token: " + e.getMessage());
        return response;
    }
}

    /**
     * Validate admin login credentials.
     * 
     * @param receivedAdmin admin credentials
     * @return response with token if valid
     */
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        
        try {
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
            
            if (admin == null) {
                response.put("error", "Admin not found");
                return ResponseEntity.status(401).body(response);
            }
            
            if (!admin.getPassword().equals(receivedAdmin.getPassword())) {
                response.put("error", "Invalid password");
                return ResponseEntity.status(401).body(response);
            }
            
            // Generate token
            String token = tokenService.generateToken(admin.getId(), "ADMIN");
            response.put("token", token);
            response.put("adminId", admin.getId().toString());
            response.put("username", admin.getUsername());
            response.put("message", "Admin login successful");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctorLogin(Login login) {
    Map<String, String> response = new HashMap<>();
    
    try {
        Doctor doctor = doctorRepository.findByEmail(login.getEmail());
        
        if (doctor == null) {
            response.put("error", "Doctor not found");
            return ResponseEntity.status(401).body(response);
        }
        
        if (!doctor.getPassword().equals(login.getPassword())) {
            response.put("error", "Invalid password");
            return ResponseEntity.status(401).body(response);
        }
        
        // Generate token
        String token = tokenService.generateToken(doctor.getId(), "DOCTOR");
        response.put("token", token);
        response.put("doctorId", doctor.getId().toString());
        response.put("name", doctor.getName());
        response.put("email", doctor.getEmail());
        response.put("message", "Doctor login successful");
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        e.printStackTrace();
        response.put("error", "Internal server error: " + e.getMessage());
        return ResponseEntity.status(500).body(response);
    }
}

    /**
     * Filter doctors by name, specialty, and available time.
     * 
     * @param name doctor name (optional)
     * @param specialty doctor specialty (optional)
     * @param time available time AM/PM (optional)
     * @return map with filtered doctors
     */
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // No filters provided - return all doctors
            if ((name == null || name.trim().isEmpty()) &&
                (specialty == null || specialty.trim().isEmpty()) &&
                (time == null || time.trim().isEmpty())) {
                
                List<Doctor> allDoctors = doctorService.getDoctors();
                response.put("doctors", allDoctors);
                response.put("count", allDoctors.size());
                response.put("message", "All doctors retrieved");
                return response;
            }
            
            // Apply filters based on provided parameters
            if (name != null && !name.trim().isEmpty() &&
                specialty != null && !specialty.trim().isEmpty() &&
                time != null && !time.trim().isEmpty()) {
                
                // All three filters
                return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
                
            } else if (name != null && !name.trim().isEmpty() &&
                       time != null && !time.trim().isEmpty()) {
                
                // Name and time filters
                return doctorService.filterDoctorByNameAndTime(name, time);
                
            } else if (name != null && !name.trim().isEmpty() &&
                       specialty != null && !specialty.trim().isEmpty()) {
                
                // Name and specialty filters
                return doctorService.filterDoctorByNameAndSpecility(name, specialty);
                
            } else if (specialty != null && !specialty.trim().isEmpty() &&
                       time != null && !time.trim().isEmpty()) {
                
                // Specialty and time filters
                return doctorService.filterDoctorByTimeAndSpecility(specialty, time);
                
            } else if (name != null && !name.trim().isEmpty()) {
                
                // Only name filter
                return doctorService.findDoctorByName(name);
                
            } else if (specialty != null && !specialty.trim().isEmpty()) {
                
                // Only specialty filter
                return doctorService.filterDoctorBySpecility(specialty);
                
            } else if (time != null && !time.trim().isEmpty()) {
                
                // Only time filter
                return doctorService.filterDoctorsByTime(time);
                
            } else {
                // Default fallback
                List<Doctor> allDoctors = doctorService.getDoctors();
                response.put("doctors", allDoctors);
                response.put("count", allDoctors.size());
                response.put("message", "All doctors retrieved");
                return response;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error filtering doctors: " + e.getMessage());
            response.put("doctors", Collections.emptyList());
            response.put("count", 0);
            return response;
        }
    }

    /**
     * Validate if an appointment time is available for a doctor.
     * 
     * @param appointment the appointment to validate
     * @return 1 if valid, 0 if unavailable, -1 if doctor doesn't exist
     */
    public int validateAppointment(Appointment appointment) {
        try {
            if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
                return -1; // Doctor not specified
            }
            
            Optional<Doctor> doctorOpt = doctorRepository.findById(appointment.getDoctor().getId());
            if (doctorOpt.isEmpty()) {
                return -1; // Doctor doesn't exist
            }
            
            Doctor doctor = doctorOpt.get();
            LocalDateTime appointmentTime = appointment.getAppointmentTime();
            LocalDate appointmentDate = appointmentTime.toLocalDate();
            
            // Get available time slots for the doctor on this date
            List<String> availableSlots = doctorService.getDoctorAvailability(doctor.getId(), appointmentDate);
            if (availableSlots.isEmpty()) {
                return 0; // No available slots
            }
            
            // Determine if appointment time falls into any available slot
            LocalTime time = appointmentTime.toLocalTime();
            String timePeriod = time.getHour() < 12 ? "AM" : "PM";
            
            if (availableSlots.contains(timePeriod)) {
                return 1; // Valid appointment time
            } else {
                return 0; // Time slot not available
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Error occurred
        }
    }

    /**
     * Validate if a patient already exists (by email or phone).
     * 
     * @param patient the patient to validate
     * @return true if patient doesn't exist (valid), false if exists
     */
    public boolean validatePatient(Patient patient) {
        try {
            Patient existingPatient = patientRepository.findByEmailOrPhone(
                    patient.getEmail(), patient.getPhone());
            return existingPatient == null; // True if no patient found
        } catch (Exception e) {
            e.printStackTrace();
            return false; // On error, assume not valid
        }
    }

    /**
     * Validate patient login credentials.
     * 
     * @param login patient login credentials
     * @return response with token if valid
     */
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        
        try {
            Patient patient = patientRepository.findByEmail(login.getEmail());
            
            if (patient == null) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(401).body(response);
            }
            
            if (!patient.getPassword().equals(login.getPassword())) {
                response.put("error", "Invalid password");
                return ResponseEntity.status(401).body(response);
            }
            
            // Generate token
            String token = tokenService.generateToken(patient.getId(), "PATIENT");
            response.put("token", token);
            response.put("patientId", patient.getId().toString());
            response.put("name", patient.getName());
            response.put("email", patient.getEmail());
            response.put("message", "Patient login successful");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Filter patient appointments by condition and/or doctor name.
     * 
     * @param condition appointment condition (past/future)
     * @param name doctor name
     * @param token patient authentication token
     * @return response with filtered appointments
     */
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract patient email from token
            String email = tokenService.extractEmail(token);
            if (email == null) {
                response.put("error", "Invalid token");
                return ResponseEntity.status(401).body(response);
            }
            
            // Get patient by email
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(404).body(response);
            }
            
            Long patientId = patient.getId();
            
            // Apply filters based on provided parameters
            if (condition != null && !condition.trim().isEmpty() &&
                name != null && !name.trim().isEmpty()) {
                
                // Both condition and doctor name filters
                return patientService.filterByDoctorAndCondition(condition, name, patientId);
                
            } else if (condition != null && !condition.trim().isEmpty()) {
                
                // Only condition filter
                return patientService.filterByCondition(condition, patientId);
                
            } else if (name != null && !name.trim().isEmpty()) {
                
                // Only doctor name filter
                return patientService.filterByDoctor(name, patientId);
                
            } else {
                
                // No filters - get all appointments
                return patientService.getPatientAppointment(patientId, token);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error filtering appointments: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get patient ID from token.
     * 
     * @param token JWT token
     * @return patient ID or null if invalid
     */
    public Long getPatientIdFromToken(String token) {
        try {
            return tokenService.extractPatientId(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get doctor ID from token.
     * 
     * @param token JWT token
     * @return doctor ID or null if invalid
     */
    public Long getDoctorIdFromToken(String token) {
        try {
            return tokenService.extractDoctorId(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if user is authorized for resource access.
     * 
     * @param token JWT token
     * @param resourceOwnerId ID of the resource owner
     * @param userType expected user type (PATIENT, DOCTOR, ADMIN)
     * @return true if authorized, false otherwise
     */
    public boolean isAuthorized(String token, Long resourceOwnerId, String userType) {
        try {
            return tokenService.validateTokenForResource(token, resourceOwnerId, userType);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get user type from token.
     * 
     * @param token JWT token
     * @return user type or null if invalid
     */
    public String getUserTypeFromToken(String token) {
        try {
            return tokenService.extractUserType(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}