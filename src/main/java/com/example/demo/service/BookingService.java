package com.example.demo.service;

import com.example.demo.entity.AppointmentAddon;
import com.example.demo.entity.ConsultationAppointment;
import com.example.demo.entity.Service;
import com.example.demo.entity.ServiceAddon;
import com.example.demo.entity.enums.AppointmentStatus;
import com.example.demo.repository.AppointmentAddonRepository;
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
    private AppointmentAddonRepository appointmentAddonRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    private static final int SLOT_DURATION_MINUTES = 60;
    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);

    // ── UC-06: Dynamic Pricing Module (DB-driven, no hard-coding) ──────
    // Base price retrieved from Service.base_price (correct schema)
    public double resolveBasePrice(Long serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found with id = " + serviceId));
        return service.getBasePrice() != null ? service.getBasePrice() : 0.0;
    }

    // List of available addons for a service, retrieved from Service_Addon table
    public List<ServiceAddon> getAddonsForService(Long serviceId) {
        return serviceAddonRepository.findByServiceId(serviceId);
    }

    // Server recalculates total addons based on addonIds sent by FE, not trusting FE price
    public double calculateAddonsPrice(Long serviceId, List<Long> addonIds) {
        if (addonIds == null || addonIds.isEmpty()) return 0.0;
        List<ServiceAddon> validAddons = serviceAddonRepository.findByServiceId(serviceId);
        Map<Long, Double> priceById = new HashMap<>();
        for (ServiceAddon a : validAddons) priceById.put(a.getId(), a.getPriceModifier());

        double sum = 0.0;
        for (Long id : addonIds) {
            Double p = priceById.get(id);
            if (p == null) {
                throw new IllegalArgumentException("Addon id " + id + " does not belong to this service");
            }
            sum += p;
        }
        return sum;
    }

    // Recalculates the add-ons total for an EXISTING appointment from the addons the client
    // actually selected (appointment_addon -> service_addon), instead of deriving it by
    // subtraction from total_price. This stays correct even after an admin edits the
    // per-booking base_price from the admin Bookings panel.
    public double calculateAddonsPriceForAppointment(Long appointmentId) {
        List<AppointmentAddon> links = appointmentAddonRepository.findByAppointmentId(appointmentId);
        if (links.isEmpty()) return 0.0;
        double sum = 0.0;
        for (AppointmentAddon link : links) {
            ServiceAddon addon = serviceAddonRepository.findById(link.getAddonId()).orElse(null);
            if (addon != null && addon.getPriceModifier() != null) {
                sum += addon.getPriceModifier();
            }
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
            throw new IllegalStateException("This timeslot has already been booked, please select a different time.");
        }
    }

    public int getSlotDurationMinutes() {
        return SLOT_DURATION_MINUTES;
    }
}