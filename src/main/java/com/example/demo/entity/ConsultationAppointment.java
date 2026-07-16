package com.example.demo.entity;

import com.example.demo.entity.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "consultation_appointment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id", nullable = false)
    private Long serviceId; // FK -> Service.id

    @Column(name = "client_id", nullable = false)
    private Long clientId; // FK -> User.id (thông tin khách hàng đã có sẵn ở User/user-profile)

    @Column(name = "expert_id", nullable = true)
    private Long expertId; // FK -> User.id (user có role ROLE_MEMBER) - KHÔNG phải bảng members, bảng đó hầu như không tham gia phân quyền hệ thống

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "time_slot", nullable = false)
    private LocalTime timeSlot; // giờ bắt đầu slot

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AppointmentStatus status;

    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    // Không có trong schema gốc nhưng cần lưu để hiển thị lại giá đã chốt
    // (server luôn tính lại theo Service.base_price + Service_Addon đã chọn)
    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = AppointmentStatus.PENDING;
    }
}