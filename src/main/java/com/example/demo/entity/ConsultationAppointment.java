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
    private Long clientId; // FK -> User.id (client info available in User/user-profile)

    @Column(name = "expert_id", nullable = true)
    private Long expertId; // FK -> User.id (user with role ROLE_MEMBER) - NOT members table, which is not used for system authorization

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "time_slot", nullable = false)
    private LocalTime timeSlot; // slot start time

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AppointmentStatus status;

    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    // Not in original schema but saved to display agreed total price
    // (server always recalculates based on Service.base_price + selected Service_Addon)
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