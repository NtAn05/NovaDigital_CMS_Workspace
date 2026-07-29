package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "quotations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_code", unique = true, nullable = false, length = 50)
    private String quoteCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private ConsultationAppointment booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 50)
    private String status = "DRAFT"; // DRAFT, PROPOSED, NEGOTIATING, APPROVED, REJECTED, CONVERTED

    @Column(length = 20)
    private String version = "1.0";

    @Column(name = "approval_token", length = 255)
    private String approvalToken;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

    @Column(name = "subtotal")
    private Double subtotal = 0.0;

    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;

    @Column(name = "tax_amount")
    private Double taxAmount = 0.0;

    @Column(name = "total_amount")
    private Double totalAmount = 0.0;

    @Column(name = "deposit_percentage")
    private Double depositPercentage = 20.0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_project_id")
    private Project convertedProject;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = "DRAFT";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
