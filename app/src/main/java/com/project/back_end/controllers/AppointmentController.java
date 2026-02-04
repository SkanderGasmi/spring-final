package com.project.back_end.controllers;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.AuthService;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AuthService authService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, AuthService authService) {
        this.appointmentService = appointmentService;
        this.authService = authService;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token) {
        
        ResponseEntity<Map<String, String>> validationResponse = authService.validateToken(token, "doctor");
        if (!validationResponse.getStatusCode().is2xxSuccessful()) {
            return validationResponse;
        }
        
        LocalDate appointmentDate = LocalDate.parse(date);
        Map<String, Object> appointments = appointmentService.getAppointment(patientName, appointmentDate, token);
        
        if (appointments.containsKey("error")) {
            return ResponseEntity.badRequest().body(appointments);
        }
        
        return ResponseEntity.ok(appointments);
    }

    @PostMapping("/{token}")
    public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenValidation = authService.validateToken(token, "patient");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            return tokenValidation;
        }
        
        int result = appointmentService.bookAppointment(appointment);
        Map<String, String> response = new java.util.HashMap<>();
        
        if (result == 1) {
            response.put("message", "Appointment booked successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Failed to book appointment");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{token}")
    public ResponseEntity<?> updateAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validationResponse = authService.validateToken(token, "patient");
        if (!validationResponse.getStatusCode().is2xxSuccessful()) {
            return validationResponse;
        }
        
        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> cancelAppointment(@PathVariable long id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validationResponse = authService.validateToken(token, "patient");
        if (!validationResponse.getStatusCode().is2xxSuccessful()) {
            return validationResponse;
        }
        
        return appointmentService.cancelAppointment(id, token);
    }
}