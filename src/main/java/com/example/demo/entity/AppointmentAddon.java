package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "appointment_addon")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId; // FK -> Consultation_Appointment.id

    @Column(name = "addon_id", nullable = false)
    private Long addonId; // FK -> Service_Addon.id
}