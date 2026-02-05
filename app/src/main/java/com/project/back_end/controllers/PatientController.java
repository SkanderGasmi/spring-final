package com.project.back_end.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.AuthService;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.TokenService;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final AuthService authService;
    private final TokenService tokenService;

    @Autowired
    public PatientController(PatientService patientService, 
                           AuthService authService,
                           TokenService tokenService) {
        this.patientService = patientService;
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getPatient(@PathVariable String token) {
        Map<String, String> validationResponse = authService.validateToken(token, "patient");
        if (validationResponse.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponse);
        }
        return patientService.getPatientDetails(token);
    }

    @PostMapping()
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
        int result = patientService.createPatient(patient);
        Map<String, String> response = new java.util.HashMap<>();
        
        if (result == 1) {
            response.put("message", "Signup successful");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Internal server error");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login login) {
        return authService.validatePatientLogin(login);
    }

    @GetMapping("/{id}/{token}")
    public ResponseEntity<?> getPatientAppointment(@PathVariable long id, @PathVariable String token) {
        Map<String, String> validationResponse = authService.validateToken(token, "patient");
        if (validationResponse.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponse);
        }
        return patientService.getPatientAppointment(id, token);
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<?> filterPatientAppointment(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {
        
        Map<String, String> validationResponse = authService.validateToken(token, "patient");
        if (validationResponse.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponse);
        }
        
        // Extract patient email from token using TokenService
        String email = tokenService.extractEmail(token);
        if (email == null) {
            Map<String, String> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Invalid token");
            return ResponseEntity.status(401).body(errorResponse);
        }
        
        // Get patient by email to get the ID
        Patient patient = patientService.getPatientByEmail(email);
        if (patient == null) {
            Map<String, String> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Patient not found");
            return ResponseEntity.status(404).body(errorResponse);
        }
        
        long patientId = patient.getId();
        
        // Check if name parameter is empty
        if (name == null || name.trim().isEmpty()) {
            // Filter by condition only
            return patientService.filterByCondition(condition, patientId);
        } else if (condition == null || condition.trim().isEmpty()) {
            // Filter by doctor name only
            return patientService.filterByDoctor(name, patientId);
        } else {
            // Filter by both condition and doctor name
            return patientService.filterByDoctorAndCondition(condition, name, patientId);
        }
    }
}