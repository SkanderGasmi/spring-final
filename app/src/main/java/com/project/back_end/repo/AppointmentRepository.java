package com.project.back_end.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Retrieve appointments for a doctor within a given time range.
     * 
     * @param doctorId the ID of the doctor
     * @param start the start time of the range
     * @param end the end time of the range
     * @return list of appointments for the doctor within the time range
     */
    @Query("SELECT a FROM Appointment a " +
           "LEFT JOIN FETCH a.doctor d " +
           "LEFT JOIN FETCH d.availableTimes " +
           "WHERE d.id = :doctorId AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Filter appointments by doctor ID, partial patient name (case-insensitive), and time range.
     * 
     * @param doctorId the ID of the doctor
     * @param patientName partial patient name to search for
     * @param start the start time of the range
     * @param end the end time of the range
     * @return list of filtered appointments
     */
    @Query("SELECT a FROM Appointment a " +
           "LEFT JOIN FETCH a.patient p " +
           "LEFT JOIN FETCH a.doctor d " +
           "WHERE a.doctor.id = :doctorId " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :patientName, '%')) " +
           "AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("patientName") String patientName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Delete all appointments related to a specific doctor.
     * 
     * @param doctorId the ID of the doctor
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Appointment a WHERE a.doctor.id = :doctorId")
    void deleteAllByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * Find all appointments for a specific patient.
     * 
     * @param patientId the ID of the patient
     * @return list of appointments for the patient
     */
    List<Appointment> findByPatientId(Long patientId);

    /**
     * Retrieve appointments for a patient by status, ordered by appointment time.
     * 
     * @param patientId the ID of the patient
     * @param status the status of the appointments
     * @return list of appointments ordered by appointment time
     */
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(
            Long patientId, 
            int status);

    /**
     * Search appointments by partial doctor name and patient ID.
     * 
     * @param doctorName partial doctor name to search for
     * @param patientId the ID of the patient
     * @return list of filtered appointments
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
           "AND a.patient.id = :patientId")
    List<Appointment> filterByDoctorNameAndPatientId(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId);

    /**
     * Filter appointments by doctor name, patient ID, and status.
     * 
     * @param doctorName partial doctor name to search for
     * @param patientId the ID of the patient
     * @param status the status of the appointments
     * @return list of filtered appointments
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
           "AND a.patient.id = :patientId " +
           "AND a.status = :status")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId,
            @Param("status") int status);

    /**
     * Update the status of a specific appointment based on its ID.
     * 
     * @param status the new status to set
     * @param id the ID of the appointment
     */
    @Modifying
    @Transactional
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
    void updateStatus(@Param("status") int status, @Param("id") long id);
}