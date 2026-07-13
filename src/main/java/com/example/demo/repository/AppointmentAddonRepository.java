package com.example.demo.repository;

import com.example.demo.entity.AppointmentAddon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentAddonRepository extends JpaRepository<AppointmentAddon, Long> {

    List<AppointmentAddon> findByAppointmentId(Long appointmentId);
}