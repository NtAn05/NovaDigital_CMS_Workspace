package com.example.demo.repository;

import com.example.demo.entity.ServiceAddon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceAddonRepository extends JpaRepository<ServiceAddon, Long> {

    List<ServiceAddon> findByServiceId(Long serviceId);
}