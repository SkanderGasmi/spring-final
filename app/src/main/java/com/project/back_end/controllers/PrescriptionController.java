package com.project.back_end.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.AuthService;
import com.project.back_end.models.Prescription;
import java.util.Map;

@RestController
@RequestMapping("${api.path}" + "prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final AuthService authService;

    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService, AuthService authService) {
        this.prescriptionService = prescriptionService;
        this.authService = authService;
    }

    @PostMapping("/{token}")
    public ResponseEntity<?> savePrescription(@RequestBody Prescription prescription, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validationResponse = authService.validateToken(token, "doctor");
        if (!validationResponse.getStatusCode().is2xxSuccessful()) {
            return validationResponse;
        }
        return prescriptionService.savePrescription(prescription);
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<?> getPrescription(@PathVariable long appointmentId, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validationResponse = authService.validateToken(token, "doctor");
        if (!validationResponse.getStatusCode().is2xxSuccessful()) {
            return validationResponse;
        }
        return prescriptionService.getPrescription(appointmentId);
    }
}