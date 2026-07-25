package com.example.demo.controller;

import com.example.demo.entity.ServiceAddon;
import com.example.demo.repository.ServiceAddonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Quản lý Service_Addon (gói thêm gắn với 1 dịch vụ). Add-on giờ được admin tạo/sửa/xoá riêng
 * ở đây (panel Services), KHÔNG còn cho khách tự chọn lúc đặt lịch nữa - admin dùng thông tin
 * add-on này làm căn cứ tham khảo khi tự quyết định giá cuối cùng cho từng booking.
 */
@RestController
@RequestMapping("/api")
public class ServiceAddonController {

    @Autowired
    private ServiceAddonRepository serviceAddonRepository;

    @GetMapping("/services/{serviceId}/addons")
    public ResponseEntity<?> getAddonsForService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(serviceAddonRepository.findByServiceId(serviceId));
    }

    @PostMapping("/service-addons")
    public ResponseEntity<?> createAddon(@RequestBody Map<String, Object> body) {
        Map<String, Object> error = new HashMap<>();
        if (body.get("serviceId") == null) { error.put("message", "Thiếu serviceId"); return ResponseEntity.badRequest().body(error); }
        if (body.get("addonName") == null || body.get("addonName").toString().isBlank()) {
            error.put("message", "Vui lòng nhập tên add-on");
            return ResponseEntity.badRequest().body(error);
        }

        ServiceAddon addon = new ServiceAddon();
        addon.setServiceId(Long.valueOf(body.get("serviceId").toString()));
        addon.setAddonName(body.get("addonName").toString());
        addon.setPriceModifier(body.get("priceModifier") != null ? Double.valueOf(body.get("priceModifier").toString()) : 0.0);

        ServiceAddon saved = serviceAddonRepository.save(addon);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/service-addons/{id}")
    public ResponseEntity<?> updateAddon(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return serviceAddonRepository.findById(id)
                .<ResponseEntity<?>>map(addon -> {
                    if (body.containsKey("addonName")) addon.setAddonName(body.get("addonName").toString());
                    if (body.containsKey("priceModifier")) addon.setPriceModifier(Double.valueOf(body.get("priceModifier").toString()));
                    return ResponseEntity.ok(serviceAddonRepository.save(addon));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/service-addons/{id}")
    public ResponseEntity<?> deleteAddon(@PathVariable Long id) {
        if (!serviceAddonRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        serviceAddonRepository.deleteById(id);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        return ResponseEntity.ok(res);
    }
}