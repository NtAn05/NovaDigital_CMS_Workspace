package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "service_addon")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id", nullable = false)
    private Long serviceId; // FK -> Service.id

    @Column(name = "addon_name", nullable = false, length = 255)
    private String addonName;

    @Column(name = "price_modifier", nullable = false)
    private Double priceModifier;
}