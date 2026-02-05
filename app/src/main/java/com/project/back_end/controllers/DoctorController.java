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
import org.springframework.web.bind.annotation.RequestParam;
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
        
        Map<String, String> validationResponse = authService.validateToken(token, user);
        if (validationResponse.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponse);
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
    public ResponseEntity<?> getDoctors() {
        var doctors = doctorService.getDoctors();
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("doctors", doctors);
        response.put("count", doctors.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{token}")
    public ResponseEntity<?> saveDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
        Map<String, String> validationResponse = authService.validateToken(token, "admin");
        if (validationResponse.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponse);
        }
        
        int result = doctorService.saveDoctor(doctor);
        Map<String, String> response = new java.util.HashMap<>();
        
        if (result == 1) {
            response.put("message", "Doctor added to db");
            return ResponseEntity.ok(response);
        } else if (result == -1) {
            response.put("error", "Doctor already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else {
            response.put("error", "Some internal error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> doctorLogin(@RequestBody Login login) {
        return authService.validateDoctorLogin(login);
    }

    @PutMapping("/{token}")
    public ResponseEntity<?> updateDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
        Map<String, String> validationResponse = authService.validateToken(token, "admin");
        if (validationResponse.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponse);
        }
        
        int result = doctorService.updateDoctor(doctor);
        Map<String, String> response = new java.util.HashMap<>();
        
        if (result == 1) {
            response.put("message", "Doctor updated");
            return ResponseEntity.ok(response);
        } else if (result == -1) {
            response.put("error", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            response.put("error", "Some internal error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> deleteDoctor(@PathVariable long id, @PathVariable String token) {
        Map<String, String> validationResponse = authService.validateToken(token, "admin");
        if (validationResponse.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponse);
        }
        
        int result = doctorService.deleteDoctor(id);
        Map<String, String> response = new java.util.HashMap<>();
        
        if (result == 1) {
            response.put("message", "Doctor deleted successfully");
            return ResponseEntity.ok(response);
        } else if (result == -1) {
            response.put("error", "Doctor not found with id");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            response.put("error", "Some internal error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String speciality) {
        
        // Handle null parameters (frontend might send empty strings)
        if (name == null) name = "";
        if (time == null) time = "";
        if (speciality == null) speciality = "";
        
        Map<String, Object> result;
        
        // Check all three parameters
        if (!name.isEmpty() && !time.isEmpty() && !speciality.isEmpty()) {
            result = doctorService.filterDoctorsByNameSpecialityAndTime(name, speciality, time);
        }
        // Check two parameters
        else if (!name.isEmpty() && !time.isEmpty()) {
            result = doctorService.filterDoctorByNameAndTime(name, time);
        } else if (!name.isEmpty() && !speciality.isEmpty()) {
            result = doctorService.filterDoctorByNameAndSpeciality(name, speciality);
        } else if (!time.isEmpty() && !speciality.isEmpty()) {
            result = doctorService.filterDoctorByTimeAndSpeciality(time, speciality);
        }
        // Check single parameter
        else if (!speciality.isEmpty()) {
            result = doctorService.filterDoctorBySpeciality(speciality);
        } else if (!time.isEmpty()) {
            result = doctorService.filterDoctorsByTime(time);
        } else if (!name.isEmpty()) {
            result = doctorService.findDoctorByName(name);
        }
        // No parameters - return all doctors
        else {
            var doctors = doctorService.getDoctors();
            result = new java.util.HashMap<>();
            result.put("doctors", doctors);
            result.put("count", doctors.size());
        }
        
        return ResponseEntity.ok(result);
    }
    
    // Additional endpoint for getting single doctor by ID (optional but useful)
    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable long id) {
        var doctor = doctorService.getDoctorById(id);
        if (doctor != null) {
            return ResponseEntity.ok(doctor);
        } else {
            Map<String, String> response = new java.util.HashMap<>();
            response.put("error", "Doctor not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}