package com.project.back_end.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Specialty is required")
    @Size(min = 3, max = 50, message = "Specialty must be between 3 and 50 characters")
    @Column(nullable = false, length = 50)
    private String specialty;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{10}", message = "Phone number must be exactly 10 digits")
    @Column(nullable = false, length = 10)
    private String phone;

    @ElementCollection
    @CollectionTable(name = "doctor_available_times", 
                     joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "time_slot", length = 20)
    private List<String> availableTimes = new ArrayList<>();

    // Relationships
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Appointment> appointments = new ArrayList<>();

    // Default constructor (required by JPA)
    public Doctor() {
    }

    // Parameterized constructor
    public Doctor(String name, String specialty, String email, String password, String phone) {
        this.name = name;
        this.specialty = specialty;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    // Helper methods

    public void addAvailableTime(String timeSlot) {
        if (availableTimes == null) {
            availableTimes = new ArrayList<>();
        }
        // Validate time slot format (optional)
        if (isValidTimeSlot(timeSlot)) {
            availableTimes.add(timeSlot);
        } else {
            throw new IllegalArgumentException("Invalid time slot format. Use format: 'HH:mm-HH:mm'");
        }
    }

    public void removeAvailableTime(String timeSlot) {
        if (availableTimes != null) {
            availableTimes.remove(timeSlot);
        }
    }

    @Transient
    public boolean hasAppointments() {
        return appointments != null && !appointments.isEmpty();
    }

    @Transient
    public long getTotalAppointments() {
        return appointments != null ? appointments.size() : 0;
    }

    @Transient
    private boolean isValidTimeSlot(String timeSlot) {
        // Basic validation for time slot format "HH:mm-HH:mm"
        return timeSlot != null && timeSlot.matches("^\\d{2}:\\d{2}-\\d{2}:\\d{2}$");
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getAvailableTimes() {
        return availableTimes;
    }

    public void setAvailableTimes(List<String> availableTimes) {
        this.availableTimes = availableTimes;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", specialty='" + specialty + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", availableTimes=" + (availableTimes != null ? availableTimes.size() : 0) + " slots" +
                '}';
    }
}