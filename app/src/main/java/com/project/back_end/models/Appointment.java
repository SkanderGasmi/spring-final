package com.project.back_end.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @NotNull(message = "Doctor is required")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "Patient is required")
    private Patient patient;

    @Column(name = "appointment_time", nullable = false)
    @Future(message = "Appointment time must be in the future")
    @NotNull(message = "Appointment time is required")
    private LocalDateTime appointmentTime;

    @Column(nullable = false)
    @NotNull(message = "Status is required")
    private int status; // 0 = Scheduled, 1 = Completed

    // Default constructor (required by JPA)
    public Appointment() {
    }

    // Parameterized constructor
    public Appointment(Doctor doctor, Patient patient, LocalDateTime appointmentTime, int status) {
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    // Transient helper methods (not persisted in database)

    @Transient
    public LocalDateTime getEndTime() {
        if (appointmentTime != null) {
            return appointmentTime.plusHours(1); // Assuming appointments are 1 hour long
        }
        return null;
    }

    @Transient
    public LocalDate getAppointmentDate() {
        if (appointmentTime != null) {
            return appointmentTime.toLocalDate();
        }
        return null;
    }

    @Transient
    public LocalTime getAppointmentTimeOnly() {
        if (appointmentTime != null) {
            return appointmentTime.toLocalTime();
        }
        return null;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    // Convenience methods for status

    @Transient
    public String getStatusText() {
        return switch (status) {
            case 0 -> "Scheduled";
            case 1 -> "Completed";
            default -> "Unknown";
        };
    }

    @Transient
    public boolean isScheduled() {
        return status == 0;
    }

    @Transient
    public boolean isCompleted() {
        return status == 1;
    }

    // Additional validation method
    @Transient
    public boolean isValid() {
        return doctor != null && patient != null && appointmentTime != null 
                && appointmentTime.isAfter(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", doctorId=" + (doctor != null ? doctor.getId() : "null") +
                ", patientId=" + (patient != null ? patient.getId() : "null") +
                ", appointmentTime=" + appointmentTime +
                ", status=" + getStatusText() +
                '}';
    }
}