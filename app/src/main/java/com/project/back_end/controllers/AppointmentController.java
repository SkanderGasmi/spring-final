package com.project.back_end.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import com.project.back_end.model.Appointment;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(@PathVariable String date, @PathVariable String patientName, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "doctor");
        if (!validationResponse.getStatusCode().is2xxSuccessful()) {
            return validationResponse;
        }
        return appointmentService.getAppointment(date, patientName);
    }

    @PostMapping("/{token}")
    public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            return tokenValidation;
        }
        ResponseEntity<Map<String, String>> appointmentValidation = service.validateAppointment(appointment);
        if (!appointmentValidation.getStatusCode().is2xxSuccessful()) {
            return appointmentValidation;
        }
        return appointmentService.bookAppointment(appointment);
    }

    @PutMapping("/{token}")
    public ResponseEntity<?> updateAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "patient");
        if (!validationResponse.getStatusCode().is2xxSuccessful()) {
            return validationResponse;
        }
        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> cancelAppointment(@PathVariable String id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "patient");
        if (!validationResponse.getStatusCode().is2xxSuccessful()) {
            return validationResponse;
        }
        return appointmentService.cancelAppointment(id);
    }
}