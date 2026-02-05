package com.project.back_end.controllers;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        
        Map<String, String> validationResponse = authService.validateToken(token, "doctor");
        if (validationResponse.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponse);
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
        Map<String, String> tokenValidation = authService.validateToken(token, "patient");
        if (tokenValidation.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tokenValidation);
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
        Map<String, String> validationResponse = authService.validateToken(token, "patient");
        if (validationResponse.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponse);
        }
        
        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> cancelAppointment(@PathVariable long id, @PathVariable String token) {
        Map<String, String> validationResponse = authService.validateToken(token, "patient");
        if (validationResponse.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponse);
        }
        
        return appointmentService.cancelAppointment(id, token);
    }
}