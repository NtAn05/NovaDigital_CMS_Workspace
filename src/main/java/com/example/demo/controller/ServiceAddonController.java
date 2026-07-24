package com.example.demo.controller;

import com.example.demo.entity.ServiceAddon;
import com.example.demo.repository.ServiceAddonRepository;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Manages the add-on packages (name + price) that belong to a Service.
// Used by the admin "Services" panel so admins can CRUD add-on pricing per service.
// Security: nested under /api/services/**, which already permits GET publicly and
// requires ADMIN or MEMBER role for POST/PUT/DELETE (see SecurityConfig).
@RestController
@RequestMapping("/api/services/{serviceId}/addons")
public class ServiceAddonController {

    @Autowired
    private ServiceAddonRepository serviceAddonRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    // ── GET ALL add-ons of a service ──────────────────────
    @GetMapping
    public ResponseEntity<?> getAddons(@PathVariable Long serviceId) {
        if (!serviceRepository.existsById(serviceId)) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Service not found with id = " + serviceId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(serviceAddonRepository.findByServiceId(serviceId));
    }

    // ── CREATE add-on for a service ───────────────────────
    @PostMapping
    public ResponseEntity<?> createAddon(@PathVariable Long serviceId, @RequestBody ServiceAddon request) {
        Map<String, Object> error = new HashMap<>();
        if (!serviceRepository.existsById(serviceId)) {
            error.put("message", "Service not found with id = " + serviceId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        if (request.getAddonName() == null || request.getAddonName().isBlank()) {
            error.put("message", "Add-on name cannot be empty");
            return ResponseEntity.badRequest().body(error);
        }
        if (request.getPriceModifier() == null || request.getPriceModifier() < 0) {
            error.put("message", "Add-on price must be a non-negative number");
            return ResponseEntity.badRequest().body(error);
        }

        ServiceAddon addon = new ServiceAddon();
        addon.setServiceId(serviceId);
        addon.setAddonName(request.getAddonName().trim());
        addon.setPriceModifier(request.getPriceModifier());

        ServiceAddon saved = serviceAddonRepository.save(addon);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── UPDATE add-on (name and/or price) ─────────────────
    @PutMapping("/{addonId}")
    public ResponseEntity<?> updateAddon(@PathVariable Long serviceId, @PathVariable Long addonId,
                                          @RequestBody ServiceAddon request) {
        Map<String, Object> error = new HashMap<>();
        Optional<ServiceAddon> optional = serviceAddonRepository.findById(addonId);
        if (optional.isEmpty() || !optional.get().getServiceId().equals(serviceId)) {
            error.put("message", "Add-on not found for this service");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        ServiceAddon addon = optional.get();
        if (request.getAddonName() != null && !request.getAddonName().isBlank()) {
            addon.setAddonName(request.getAddonName().trim());
        }
        if (request.getPriceModifier() != null) {
            if (request.getPriceModifier() < 0) {
                error.put("message", "Add-on price must be a non-negative number");
                return ResponseEntity.badRequest().body(error);
            }
            addon.setPriceModifier(request.getPriceModifier());
        }

        ServiceAddon saved = serviceAddonRepository.save(addon);
        return ResponseEntity.ok(saved);
    }

    // ── DELETE add-on ──────────────────────────────────────
    @DeleteMapping("/{addonId}")
    public ResponseEntity<?> deleteAddon(@PathVariable Long serviceId, @PathVariable Long addonId) {
        Optional<ServiceAddon> optional = serviceAddonRepository.findById(addonId);
        if (optional.isEmpty() || !optional.get().getServiceId().equals(serviceId)) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Add-on not found for this service");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        serviceAddonRepository.deleteById(addonId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Add-on deleted successfully");
        return ResponseEntity.ok(result);
    }
}
