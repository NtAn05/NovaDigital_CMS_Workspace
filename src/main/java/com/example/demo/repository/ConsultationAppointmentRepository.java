package com.example.demo.repository;

import com.example.demo.entity.ConsultationAppointment;
import com.example.demo.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ConsultationAppointmentRepository extends JpaRepository<ConsultationAppointment, Long> {

    // Get active (non-cancelled) appointments for an expert on a specific date
    // -> used to lock booked timeslots on calendar (UC-07)
    List<ConsultationAppointment> findByExpertIdAndAppointmentDateAndStatusNot(
            Long expertId, LocalDate appointmentDate, AppointmentStatus status);


    // Get all bookings for a client
    List<ConsultationAppointment> findByClientIdOrderByAppointmentDateDesc(Long clientId);
    // Get all bookings assigned to an expert -> used for "My Consultation Bookings" of member
    List<ConsultationAppointment> findByExpertIdOrderByAppointmentDateDesc(Long expertId);
}