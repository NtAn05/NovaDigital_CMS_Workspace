package com.example.demo.controller;

import com.example.demo.dto.ServiceResponse;
import com.example.demo.entity.Service;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.service.ServicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServicesService servicesService;

    @Autowired
    private ServiceRepository serviceRepository;

    // ── GET ALL ──────────────────────────────────────────
    @GetMapping
    public List<ServiceResponse> getAllServices() {
        return servicesService.getAllServices().stream().map(s -> {
            ServiceResponse r = new ServiceResponse();
            r.setId(s.getId());
            r.setTitle(s.getTitle());
            r.setDescription(s.getDescription());
            r.setIconUrl(s.getIconUrl());
            return r;
        }).collect(Collectors.toList());
    }

    // ── CREATE ───────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createService(@RequestBody Service request) {
        Map<String, Object> error = new HashMap<>();
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            error.put("message", "Tên dịch vụ không được để trống");
            return ResponseEntity.badRequest().body(error);
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            error.put("message", "Mô tả không được để trống");
            return ResponseEntity.badRequest().body(error);
        }

        Service service = new Service();
        service.setTitle(request.getTitle().trim());
        service.setDescription(request.getDescription().trim());
        service.setIconUrl(request.getIconUrl() != null ? request.getIconUrl() : "web");

        Service saved = serviceRepository.save(service);
        ServiceResponse resp = new ServiceResponse();
        resp.setId(saved.getId()); resp.setTitle(saved.getTitle());
        resp.setDescription(saved.getDescription()); resp.setIconUrl(saved.getIconUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // ── UPDATE ───────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updateService(@PathVariable Long id, @RequestBody Service request) {
        Map<String, Object> error = new HashMap<>();
        Optional<Service> optional = serviceRepository.findById(id);
        if (optional.isEmpty()) {
            error.put("message", "Không tìm thấy dịch vụ với id = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Service service = optional.get();
        if (request.getTitle()       != null && !request.getTitle().isBlank())       service.setTitle(request.getTitle().trim());
        if (request.getDescription() != null && !request.getDescription().isBlank()) service.setDescription(request.getDescription().trim());
        if (request.getIconUrl()     != null && !request.getIconUrl().isBlank())     service.setIconUrl(request.getIconUrl());

        Service saved = serviceRepository.save(service);
        ServiceResponse resp = new ServiceResponse();
        resp.setId(saved.getId()); resp.setTitle(saved.getTitle());
        resp.setDescription(saved.getDescription()); resp.setIconUrl(saved.getIconUrl());
        return ResponseEntity.ok(resp);
    }

    // ── DELETE ───────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        if (!serviceRepository.existsById(id)) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Không tìm thấy dịch vụ với id = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        serviceRepository.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Đã xóa dịch vụ thành công");
        return ResponseEntity.ok(result);
    }
}
