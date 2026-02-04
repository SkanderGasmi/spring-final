package com.project.back_end.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Document(collection = "prescriptions")
public class Prescription {

    @Id
    private String id;

    @NotBlank(message = "Patient name is required")
    @Size(min = 3, max = 100, message = "Patient name must be between 3 and 100 characters")
    @Field("patient_name")
    private String patientName;

    @NotNull(message = "Appointment ID is required")
    @Field("appointment_id")
    private Long appointmentId;

    @NotBlank(message = "Medication is required")
    @Size(min = 3, max = 100, message = "Medication must be between 3 and 100 characters")
    private String medication;

    @NotBlank(message = "Dosage is required")
    @Size(min = 3, max = 20, message = "Dosage must be between 3 and 20 characters")
    private String dosage;

    @Size(max = 200, message = "Doctor notes cannot exceed 200 characters")
    @Field("doctor_notes")
    private String doctorNotes;

    @Field("prescribed_date")
    private LocalDateTime prescribedDate;

    @Field("duration_days")
    private Integer durationDays;

    @Field("refills")
    private Integer refills = 0;

    @Field("is_active")
    private Boolean isActive = true;

    @Field("doctor_id")
    private Long doctorId;

    // Timestamps
    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public Prescription() {
        this.prescribedDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Parameterized constructor for essential fields
    public Prescription(String patientName, Long appointmentId, String medication, String dosage) {
        this();
        this.patientName = patientName;
        this.appointmentId = appointmentId;
        this.medication = medication;
        this.dosage = dosage;
    }

    // Full parameterized constructor
    public Prescription(String patientName, Long appointmentId, String medication, 
                       String dosage, String doctorNotes, Integer durationDays, 
                       Integer refills, Long doctorId) {
        this(patientName, appointmentId, medication, dosage);
        this.doctorNotes = doctorNotes;
        this.durationDays = durationDays;
        this.refills = refills;
        this.doctorId = doctorId;
    }

    // Helper methods

    public boolean hasRefills() {
        return refills != null && refills > 0;
    }

    public boolean isExpired() {
        if (prescribedDate == null || durationDays == null) return false;
        return prescribedDate.plusDays(durationDays).isBefore(LocalDateTime.now());
    }

    public int getRemainingRefills() {
        return refills != null ? refills : 0;
    }

    // Builder pattern (optional but useful)
    public static class Builder {
        private String patientName;
        private Long appointmentId;
        private String medication;
        private String dosage;
        private String doctorNotes;
        private Integer durationDays;
        private Integer refills;
        private Long doctorId;

        public Builder patientName(String patientName) {
            this.patientName = patientName;
            return this;
        }

        public Builder appointmentId(Long appointmentId) {
            this.appointmentId = appointmentId;
            return this;
        }

        public Builder medication(String medication) {
            this.medication = medication;
            return this;
        }

        public Builder dosage(String dosage) {
            this.dosage = dosage;
            return this;
        }

        public Builder doctorNotes(String doctorNotes) {
            this.doctorNotes = doctorNotes;
            return this;
        }

        public Builder durationDays(Integer durationDays) {
            this.durationDays = durationDays;
            return this;
        }

        public Builder refills(Integer refills) {
            this.refills = refills;
            return this;
        }

        public Builder doctorId(Long doctorId) {
            this.doctorId = doctorId;
            return this;
        }

        public Prescription build() {
            return new Prescription(patientName, appointmentId, medication, dosage, 
                                   doctorNotes, durationDays, refills, doctorId);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getMedication() {
        return medication;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    public LocalDateTime getPrescribedDate() {
        return prescribedDate;
    }

    public void setPrescribedDate(LocalDateTime prescribedDate) {
        this.prescribedDate = prescribedDate;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Integer getRefills() {
        return refills;
    }

    public void setRefills(Integer refills) {
        this.refills = refills;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "id='" + id + '\'' +
                ", patientName='" + patientName + '\'' +
                ", appointmentId=" + appointmentId +
                ", medication='" + medication + '\'' +
                ", dosage='" + dosage + '\'' +
                ", prescribedDate=" + prescribedDate +
                ", isActive=" + isActive +
                '}';
    }
}