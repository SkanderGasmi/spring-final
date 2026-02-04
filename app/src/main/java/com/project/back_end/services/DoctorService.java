package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Get available time slots for a specific doctor on a given date.
     * 
     * @param doctorId the ID of the doctor
     * @param date the date to check availability for
     * @return list of available time slots
     */
    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Doctor doctor = doctorOpt.get();
        List<String> allSlots = doctor.getAvailableTimes();
        if (allSlots == null || allSlots.isEmpty()) {
            return Collections.emptyList();
        }

        // Get booked appointments for the day
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);

        // Extract booked time slots
        Set<String> bookedSlots = appointments.stream()
                .map(appointment -> {
                    LocalTime time = appointment.getAppointmentTime().toLocalTime();
                    int hour = time.getHour();
                    return hour < 12 ? "AM" : "PM";
                })
                .collect(Collectors.toSet());

        // Filter available slots
        return allSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());
    }

    /**
     * Save a new doctor to the database.
     * 
     * @param doctor the doctor to save
     * @return 1 for success, -1 if doctor exists, 0 for internal error
     */
    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            // Check if doctor already exists by email
            Doctor existingDoctor = doctorRepository.findByEmail(doctor.getEmail());
            if (existingDoctor != null) {
                return -1; // Doctor already exists
            }
            
            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Internal error
        }
    }

    /**
     * Update an existing doctor's details.
     * 
     * @param doctor the doctor with updated details
     * @return 1 for success, -1 if doctor not found, 0 for internal error
     */
    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            if (!doctorRepository.existsById(doctor.getId())) {
                return -1; // Doctor not found
            }
            
            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Internal error
        }
    }

    /**
     * Get all doctors from the database.
     * 
     * @return list of all doctors
     */
    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        // Eagerly load available times to avoid lazy loading issues
        doctors.forEach(doctor -> {
            if (doctor.getAvailableTimes() != null) {
                doctor.getAvailableTimes().size(); // Trigger loading
            }
        });
        return doctors;
    }

    /**
     * Delete a doctor by ID.
     * 
     * @param id the ID of the doctor to delete
     * @return 1 for success, -1 if doctor not found, 0 for internal error
     */
    @Transactional
    public int deleteDoctor(long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1; // Doctor not found
            }
            
            // Delete all appointments for this doctor first
            appointmentRepository.deleteAllByDoctorId(id);
            
            // Then delete the doctor
            doctorRepository.deleteById(id);
            return 1; // Success
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Internal error
        }
    }

    /**
     * Validate a doctor's login credentials.
     * 
     * @param login the login credentials
     * @return response entity with token or error message
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();
        
        Doctor doctor = doctorRepository.findByEmail(login.getEmail());
        if (doctor == null) {
            response.put("error", "Doctor not found");
            return ResponseEntity.status(404).body(response);
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
        
        return ResponseEntity.ok(response);
    }

    /**
     * Find doctors by name.
     * 
     * @param name the name to search for
     * @return map with list of matching doctors
     */
    @Transactional(readOnly = true)
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        
        // Eagerly load available times
        doctors.forEach(doctor -> {
            if (doctor.getAvailableTimes() != null) {
                doctor.getAvailableTimes().size();
            }
        });
        
        response.put("doctors", doctors);
        response.put("count", doctors.size());
        return response;
    }

    /**
     * Filter doctors by name, specialty, and time availability.
     * 
     * @param name doctor's name (can be partial)
     * @param specialty doctor's specialty
     * @param amOrPm time of day: AM/PM
     * @return map with filtered doctors
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        
        List<Doctor> doctors = doctorRepository
                .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        
        List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);
        
        response.put("doctors", filteredDoctors);
        response.put("count", filteredDoctors.size());
        response.put("filters", Map.of("name", name, "specialty", specialty, "time", amOrPm));
        
        return response;
    }

    /**
     * Filter doctors by name and time availability.
     * 
     * @param name doctor's name (can be partial)
     * @param amOrPm time of day: AM/PM
     * @return map with filtered doctors
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);
        
        response.put("doctors", filteredDoctors);
        response.put("count", filteredDoctors.size());
        response.put("filters", Map.of("name", name, "time", amOrPm));
        
        return response;
    }

    /**
     * Filter doctors by name and specialty.
     * 
     * @param name doctor's name (can be partial)
     * @param specialty doctor's specialty
     * @return map with filtered doctors
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        Map<String, Object> response = new HashMap<>();
        
        List<Doctor> doctors = doctorRepository
                .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        
        response.put("doctors", doctors);
        response.put("count", doctors.size());
        response.put("filters", Map.of("name", name, "specialty", specialty));
        
        return response;
    }

    /**
     * Filter doctors by specialty and time availability.
     * 
     * @param specialty doctor's specialty
     * @param amOrPm time of day: AM/PM
     * @return map with filtered doctors
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specialty, String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);
        
        response.put("doctors", filteredDoctors);
        response.put("count", filteredDoctors.size());
        response.put("filters", Map.of("specialty", specialty, "time", amOrPm));
        
        return response;
    }

    /**
     * Filter doctors by specialty.
     * 
     * @param specialty doctor's specialty
     * @return map with filtered doctors
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        Map<String, Object> response = new HashMap<>();
        
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        
        response.put("doctors", doctors);
        response.put("count", doctors.size());
        response.put("filters", Map.of("specialty", specialty));
        
        return response;
    }

    /**
     * Filter doctors by time availability.
     * 
     * @param amOrPm time of day: AM/PM
     * @return map with filtered doctors
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        
        List<Doctor> allDoctors = doctorRepository.findAll();
        List<Doctor> filteredDoctors = filterDoctorByTime(allDoctors, amOrPm);
        
        response.put("doctors", filteredDoctors);
        response.put("count", filteredDoctors.size());
        response.put("filters", Map.of("time", amOrPm));
        
        return response;
    }

    /**
     * Private helper method to filter doctors by time availability.
     * 
     * @param doctors list of doctors to filter
     * @param amOrPm time of day: AM/PM
     * @return filtered list of doctors
     */
    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (amOrPm == null || amOrPm.trim().isEmpty()) {
            return doctors;
        }
        
        String timePeriod = amOrPm.trim().toUpperCase();
        if (!timePeriod.equals("AM") && !timePeriod.equals("PM")) {
            return Collections.emptyList();
        }
        
        return doctors.stream()
                .filter(doctor -> {
                    List<String> availableTimes = doctor.getAvailableTimes();
                    if (availableTimes == null || availableTimes.isEmpty()) {
                        return false;
                    }
                    return availableTimes.stream()
                            .anyMatch(time -> time.equalsIgnoreCase(timePeriod));
                })
                .collect(Collectors.toList());
    }

    /**
     * Get doctor by email.
     * 
     * @param email the email to search for
     * @return the doctor or null if not found
     */
    @Transactional(readOnly = true)
    public Doctor getDoctorByEmail(String email) {
        return doctorRepository.findByEmail(email);
    }

    /**
     * Get doctor by ID.
     * 
     * @param id the doctor ID
     * @return the doctor or null if not found
     */
    @Transactional(readOnly = true)
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id).orElse(null);
    }

    /**
     * Check if doctor exists by email.
     * 
     * @param email the email to check
     * @return true if doctor exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean doctorExistsByEmail(String email) {
        return doctorRepository.findByEmail(email) != null;
    }
}