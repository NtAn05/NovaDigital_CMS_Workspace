package com.example.demo.service;

import com.example.demo.entity.ConsultationAppointment;
import com.example.demo.entity.Service;
import com.example.demo.entity.ServiceAddon;
import com.example.demo.entity.enums.AppointmentStatus;
import com.example.demo.repository.ConsultationAppointmentRepository;
import com.example.demo.repository.ServiceAddonRepository;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class BookingService {

    @Autowired
    private ConsultationAppointmentRepository appointmentRepository;

    @Autowired
    private ServiceAddonRepository serviceAddonRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    private static final int SLOT_DURATION_MINUTES = 60;
    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);

    // ── UC-06: Dynamic Pricing Module (DB-driven, không hard-code) ──────
    // Giá gốc lấy từ Service.base_price (đúng schema)
    public double resolveBasePrice(Long serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found with id = " + serviceId));
        return service.getBasePrice() != null ? service.getBasePrice() : 0.0;
    }

    // Danh sách addon khả dụng của 1 service, lấy từ bảng Service_Addon
    public List<ServiceAddon> getAddonsForService(Long serviceId) {
        return serviceAddonRepository.findByServiceId(serviceId);
    }

    // Server tự tính lại tổng addon dựa trên addonIds do FE gửi lên, không tin price của FE
    public double calculateAddonsPrice(Long serviceId, List<Long> addonIds) {
        if (addonIds == null || addonIds.isEmpty()) return 0.0;
        List<ServiceAddon> validAddons = serviceAddonRepository.findByServiceId(serviceId);
        Map<Long, Double> priceById = new HashMap<>();
        for (ServiceAddon a : validAddons) priceById.put(a.getId(), a.getPriceModifier());

        double sum = 0.0;
        for (Long id : addonIds) {
            Double p = priceById.get(id);
            if (p == null) {
                throw new IllegalArgumentException("Addon id " + id + " không thuộc service này");
            }
            sum += p;
        }
        return sum;
    }

    // ── UC-07: Booking Calendar Controls ─────────────────────────────
    public List<Map<String, Object>> getDaySlots(Long expertId, LocalDate date) {
        List<ConsultationAppointment> existing = appointmentRepository
                .findByExpertIdAndAppointmentDateAndStatusNot(expertId, date, AppointmentStatus.CANCELLED);

        List<Map<String, Object>> slots = new java.util.ArrayList<>();
        LocalTime cursor = WORK_START;
        while (cursor.isBefore(WORK_END)) {
            LocalTime slotEnd = cursor.plusMinutes(SLOT_DURATION_MINUTES);
            final LocalTime start = cursor;
            boolean locked = existing.stream().anyMatch(a -> overlaps(start, slotEnd, a.getTimeSlot(), a.getTimeSlot().plusMinutes(SLOT_DURATION_MINUTES)));

            Map<String, Object> slot = new HashMap<>();
            slot.put("startTime", start.toString());
            slot.put("endTime", slotEnd.toString());
            slot.put("locked", locked);
            slots.add(slot);

            cursor = slotEnd;
        }
        return slots;
    }

    private boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    public void assertSlotAvailable(Long expertId, LocalDate date, LocalTime start, LocalTime end) {
        List<ConsultationAppointment> existing = appointmentRepository
                .findByExpertIdAndAppointmentDateAndStatusNot(expertId, date, AppointmentStatus.CANCELLED);
        boolean clash = existing.stream()
                .anyMatch(a -> overlaps(start, end, a.getTimeSlot(), a.getTimeSlot().plusMinutes(SLOT_DURATION_MINUTES)));
        if (clash) {
            throw new IllegalStateException("Khung giờ này đã có người đặt, vui lòng chọn khung giờ khác.");
        }
    }

    public int getSlotDurationMinutes() {
        return SLOT_DURATION_MINUTES;
    }
}