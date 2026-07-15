package com.example.demo.repository;

import com.example.demo.entity.ConsultationAppointment;
import com.example.demo.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ConsultationAppointmentRepository extends JpaRepository<ConsultationAppointment, Long> {

    // Lấy các lịch hẹn còn hiệu lực (chưa hủy) của 1 expert trong 1 ngày
    // -> dùng để khóa timeslot đã đặt trên calendar (UC-07)
    List<ConsultationAppointment> findByExpertIdAndAppointmentDateAndStatusNot(
            Long expertId, LocalDate appointmentDate, AppointmentStatus status);

    // Lấy toàn bộ booking được gán cho 1 expert -> dùng cho "My Consultation Bookings" của member
    List<ConsultationAppointment> findByExpertIdOrderByAppointmentDateDesc(Long expertId);
}