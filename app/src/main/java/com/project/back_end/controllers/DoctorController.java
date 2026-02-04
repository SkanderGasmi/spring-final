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

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.AuthService;
import com.project.back_end.services.DoctorService;

@RestController
@RequestMapping("${api.path}" + "doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final AuthService authService;

    @Autowired
    public DoctorController(DoctorService doctorService, AuthService authService) {
        this.doctorService = doctorService;
        this.authService = authService;
    }

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<?> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable long doctorId,
            @PathVariable String date,
            @PathVariable String token) {
        
        ResponseEntity<Map<String, String>> validationResponse = authService.validateToken(token, user);
        if (!validationResponse.getStatusCode().is2xxSuccessful()) {
            return validationResponse;
        }
        
        LocalDate appointmentDate = LocalDate.parse(date);
        var availability = doctorService.getDoctorAvailability(doctorId, appointmentDate);
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("doctorId", doctorId);
        response.put("date", date);
        response.put("availableSlots", availability);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getDoctor() {
        var doctors = doctorService.getDoctors();
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("doctors", doctors);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{token}")
    public ResponseEntity<?> saveDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validationResponse = authService.validateToken(token, "admin");
        if (!validationResponse.getStatusCode().is2xxSuccessful()) {
            return validationResponse;
        }
        
        int result = doctorService.saveDoctor(doctor);
        Map<String, String> response = new java.util.HashMap<>();
        
        if (result == 1) {
            response.put("message", "Doctor added to db");
            return ResponseEntity.ok(response);
        } else if (result == -1) {
            response.put("error", "Doctor already exists");
            return ResponseEntity.status(409).body(response);
        } else {
            response.put("error", "Some internal error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    @PutMapping("/{token}")
    public ResponseEntity<?> updateDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validationResponse = authService.validateToken(token, "admin");
        if (!validationResponse.getStatusCode().is2xxSuccessful()) {
            return validationResponse;
        }
        
        int result = doctorService.updateDoctor(doctor);
        Map<String, String> response = new java.util.HashMap<>();
        
        if (result == 1) {
            response.put("message", "Doctor updated");
            return ResponseEntity.ok(response);
        } else if (result == -1) {
            response.put("error", "Doctor not found");
            return ResponseEntity.status(404).body(response);
        } else {
            response.put("error", "Some internal error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> deleteDoctor(@PathVariable long id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> validationResponse = authService.validateToken(token, "admin");
        if (!validationResponse.getStatusCode().is2xxSuccessful()) {
            return validationResponse;
        }
        
        int result = doctorService.deleteDoctor(id);
        Map<String, String> response = new java.util.HashMap<>();
        
        if (result == 1) {
            response.put("message", "Doctor deleted successfully");
            return ResponseEntity.ok(response);
        } else if (result == -1) {
            response.put("error", "Doctor not found with id");
            return ResponseEntity.status(404).body(response);
        } else {
            response.put("error", "Some internal error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<?> filter(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {
        
        Map<String, Object> result;
        
        if (!name.isEmpty() && !time.isEmpty() && !speciality.isEmpty()) {
            result = doctorService.filterDoctorsByNameSpecilityandTime(name, speciality, time);
        } else if (!name.isEmpty() && !time.isEmpty()) {
            result = doctorService.filterDoctorByNameAndTime(name, time);
        } else if (!name.isEmpty() && !speciality.isEmpty()) {
            result = doctorService.filterDoctorByNameAndSpecility(name, speciality);
        } else if (!time.isEmpty() && !speciality.isEmpty()) {
            result = doctorService.filterDoctorByTimeAndSpecility(speciality, time);
        } else if (!speciality.isEmpty()) {
            result = doctorService.filterDoctorBySpecility(speciality);
        } else if (!time.isEmpty()) {
            result = doctorService.filterDoctorsByTime(time);
        } else if (!name.isEmpty()) {
            result = doctorService.findDoctorByName(name);
        } else {
            // If all parameters are empty, return all doctors
            var doctors = doctorService.getDoctors();
            result = new java.util.HashMap<>();
            result.put("doctors", doctors);
            result.put("count", doctors.size());
        }
        
        return ResponseEntity.ok(result);
    }
}