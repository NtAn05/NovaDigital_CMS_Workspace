package com.example.demo.controller;

import com.example.demo.dto.BookingRequest;
import com.example.demo.dto.BookingResponse;
import com.example.demo.entity.AppointmentAddon;
import com.example.demo.entity.ConsultationAppointment;
import com.example.demo.entity.ServiceAddon;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Service;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.AppointmentStatus;
import com.example.demo.repository.AppointmentAddonRepository;
import com.example.demo.repository.ConsultationAppointmentRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.service.BookingService;
import com.example.demo.service.CaptchaService;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ConsultationAppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentAddonRepository appointmentAddonRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaptchaService captchaService;

    // ── Chống spam: sinh captcha ký tự mới ──
    @GetMapping("/captcha")
    public ResponseEntity<?> getCaptcha() {
        return ResponseEntity.ok(captchaService.generateCaptcha());
    }

    // ── UC-06: giá gốc + danh sách Service_Addon của 1 service (DB-driven) ──
    @GetMapping("/pricing")
    public ResponseEntity<?> getPricing(@RequestParam Long serviceId) {
        try {
            double basePrice = bookingService.resolveBasePrice(serviceId);
            List<ServiceAddon> addons = bookingService.getAddonsForService(serviceId);

            Map<String, Object> result = new HashMap<>();
            result.put("basePrice", basePrice);
            result.put("addons", addons.stream().map(a -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", a.getId());
                m.put("addonName", a.getAddonName());
                m.put("priceModifier", a.getPriceModifier());
                return m;
            }).collect(Collectors.toList()));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ── UC-07: slot trong ngày của 1 expert, đánh dấu slot đã khóa ──
    @GetMapping("/slots")
    public ResponseEntity<?> getDaySlots(@RequestParam Long expertId,
                                          @RequestParam String date) {
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            List<Map<String, Object>> slots = bookingService.getDaySlots(expertId, parsedDate);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Ngày không hợp lệ, định dạng cần là yyyy-MM-dd");
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ── CREATE: tạo Consultation_Appointment, server tính lại giá + chặn trùng lịch ──
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        Map<String, Object> error = new HashMap<>();

        // Chống spam: bắt buộc captcha đúng mới cho tạo booking (token dùng 1 lần, tự hết hạn sau 5 phút)
        if (!captchaService.validateCaptcha(request.getCaptchaToken(), request.getCaptchaAnswer())) {
            error.put("message", "Mã xác nhận không đúng hoặc đã hết hạn, vui lòng thử lại.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (request.getServiceId() == null) { error.put("message", "Vui lòng chọn dịch vụ"); return ResponseEntity.badRequest().body(error); }
        if (request.getClientId() == null) { error.put("message", "Thiếu client_id (User)"); return ResponseEntity.badRequest().body(error); }
        if (request.getExpertId() == null) { error.put("message", "Vui lòng chọn chuyên gia tư vấn"); return ResponseEntity.badRequest().body(error); }
        if (request.getAppointmentDate() == null || request.getTimeSlot() == null) {
            error.put("message", "Vui lòng chọn ngày và khung giờ tư vấn");
            return ResponseEntity.badRequest().body(error);
        }

        LocalDate appointmentDate;
        LocalTime timeSlot;
        try {
            appointmentDate = LocalDate.parse(request.getAppointmentDate());
            timeSlot = LocalTime.parse(request.getTimeSlot());
        } catch (Exception e) {
            error.put("message", "Định dạng ngày/giờ không hợp lệ");
            return ResponseEntity.badRequest().body(error);
        }
        LocalTime endTime = timeSlot.plusMinutes(bookingService.getSlotDurationMinutes());

        // UC-07: chặn overlap - kiểm tra lại phía server
        try {
            bookingService.assertSlotAvailable(request.getExpertId(), appointmentDate, timeSlot, endTime);
        } catch (IllegalStateException e) {
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        // UC-06: tính giá lại phía server từ Service.base_price + Service_Addon
        double basePrice;
        double addonsPrice;
        try {
            basePrice = bookingService.resolveBasePrice(request.getServiceId());
            addonsPrice = bookingService.calculateAddonsPrice(request.getServiceId(), request.getAddonIds());
        } catch (IllegalArgumentException e) {
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
        double totalPrice = basePrice + addonsPrice;

        ConsultationAppointment appointment = new ConsultationAppointment();
        appointment.setServiceId(request.getServiceId());
        appointment.setClientId(request.getClientId());
        appointment.setExpertId(request.getExpertId());
        appointment.setAppointmentDate(appointmentDate);
        appointment.setTimeSlot(timeSlot);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setMessageContent(request.getMessageContent());
        appointment.setAttachmentUrl(request.getAttachmentUrl());
        appointment.setTotalPrice(totalPrice);

        ConsultationAppointment saved = appointmentRepository.save(appointment);

        if (request.getAddonIds() != null) {
            for (Long addonId : request.getAddonIds()) {
                AppointmentAddon link = new AppointmentAddon();
                link.setAppointmentId(saved.getId());
                link.setAddonId(addonId);
                appointmentAddonRepository.save(link);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved, basePrice, addonsPrice, request.getAddonIds()));
    }

    @GetMapping
    public ResponseEntity<?> getAllBookings() {
        List<ConsultationAppointment> appointments = appointmentRepository.findAll();
        List<BookingResponse> responses = appointments.stream().map(a -> {
            List<Long> addonIds = appointmentAddonRepository.findByAppointmentId(a.getId())
                    .stream().map(AppointmentAddon::getAddonId).collect(Collectors.toList());
            double basePrice = 0;
            try {
                basePrice = bookingService.resolveBasePrice(a.getServiceId());
            } catch (Exception e) {}
            double addonsPrice = a.getTotalPrice() - basePrice;
            return toResponse(a, basePrice, addonsPrice, addonIds);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return appointmentRepository.findById(id)
                .<ResponseEntity<?>>map(a -> {
                    AppointmentStatus statusBefore = a.getStatus();

                    if (body.containsKey("expertId")) {
                        Object expId = body.get("expertId");
                        if (expId != null && !expId.toString().isBlank()) {
                            a.setExpertId(Long.valueOf(expId.toString()));
                        } else {
                            a.setExpertId(null);
                        }
                    }
                    if (body.containsKey("status")) {
                        a.setStatus(AppointmentStatus.valueOf(body.get("status").toString()));
                    }
                    ConsultationAppointment saved = appointmentRepository.save(a);

                    // Vừa chuyển sang CONFIRMED (không phải đã confirmed từ trước) và đã có expert được gán
                    // -> báo cho member đó biết, kèm tên khách hàng thật
                    boolean justConfirmed = saved.getStatus() == AppointmentStatus.CONFIRMED
                            && statusBefore != AppointmentStatus.CONFIRMED;
                    if (justConfirmed && saved.getExpertId() != null) {
                        notifyExpertBookingConfirmed(saved);
                    }
                    if (justConfirmed) {
                        notifyClientBookingConfirmed(saved);
                    }

                    List<Long> addonIds = appointmentAddonRepository.findByAppointmentId(saved.getId())
                            .stream().map(AppointmentAddon::getAddonId).collect(Collectors.toList());
                    double basePrice = 0;
                    try {
                        basePrice = bookingService.resolveBasePrice(saved.getServiceId());
                    } catch (Exception e) {}
                    double addonsPrice = saved.getTotalPrice() - basePrice;
                    return ResponseEntity.ok(toResponse(saved, basePrice, addonsPrice, addonIds));
                })
                .orElseGet(() -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("message", "Appointment not found with id = " + id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                });
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBooking(@PathVariable Long id) {
        return appointmentRepository.findById(id)
                .<ResponseEntity<?>>map(a -> {
                    List<Long> addonIds = appointmentAddonRepository.findByAppointmentId(a.getId())
                            .stream().map(AppointmentAddon::getAddonId).collect(Collectors.toList());
                    double basePrice = bookingService.resolveBasePrice(a.getServiceId());
                    double addonsPrice = a.getTotalPrice() - basePrice;
                    return ResponseEntity.ok(toResponse(a, basePrice, addonsPrice, addonIds));
                })
                .orElseGet(() -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("message", "Appointment not found with id = " + id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        return appointmentRepository.findById(id)
                .<ResponseEntity<?>>map(a -> {
                    List<AppointmentAddon> addons = appointmentAddonRepository.findByAppointmentId(id);
                    appointmentAddonRepository.deleteAll(addons);
                    appointmentRepository.delete(a);
                    Map<String, Object> res = new HashMap<>();
                    res.put("success", true);
                    res.put("message", "Deleted successfully");
                    return ResponseEntity.ok(res);
                })
                .orElseGet(() -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("message", "Appointment not found with id = " + id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                });
    }

    /**
     * Tạo thông báo trong app cho member được gán khi booking chuyển sang CONFIRMED,
     * hiện đúng tên khách hàng thật (không phải "Khách hàng #123").
     *
     * Lưu ý: expert_id trỏ thẳng về User.id (user có role ROLE_MEMBER), KHÔNG phải Member.id -
     * vì bảng members hầu như không tham gia vào phân quyền/đăng nhập của hệ thống, chỉ có users mới có.
     */
    private void notifyExpertBookingConfirmed(ConsultationAppointment appointment) {
        Long expertUserId = appointment.getExpertId(); // đã là User.id, không cần tra qua Member nữa
        if (expertUserId == null) return;

        String customerName = userRepository.findById(appointment.getClientId())
                .map(User::getFullName)
                .orElse("Khách hàng #" + appointment.getClientId());

        String serviceTitle = serviceRepository.findById(appointment.getServiceId())
                .map(Service::getTitle)
                .orElse("dịch vụ #" + appointment.getServiceId());

        Notification noti = new Notification();
        noti.setUserId(expertUserId);
        noti.setTitle("You've been assigned a new consultation booking");
        noti.setMessage(String.format(
                "Customer %s's consultation \"%s\" has been confirmed for %s at %s.",
                customerName, serviceTitle, appointment.getAppointmentDate(),
                appointment.getTimeSlot().toString().substring(0, 5)
        ));
        noti.setLink("member-contact.html#my-bookings");
        notificationRepository.save(noti);
    }

    /**
     * Tạo thông báo trong app cho khách hàng (ROLE_USER, chính là clientId) khi booking
     * họ đặt được xác nhận (CONFIRMED), kèm tên chuyên gia phụ trách nếu đã được gán.
     */
    private void notifyClientBookingConfirmed(ConsultationAppointment appointment) {
        Long clientUserId = appointment.getClientId();
        if (clientUserId == null) return;

        String serviceTitle = serviceRepository.findById(appointment.getServiceId())
                .map(Service::getTitle)
                .orElse("dịch vụ #" + appointment.getServiceId());

        String expertName = appointment.getExpertId() != null
                ? userRepository.findById(appointment.getExpertId())
                        .map(User::getFullName)
                        .orElse(null)
                : null;

        Notification noti = new Notification();
        noti.setUserId(clientUserId);
        noti.setTitle("Your consultation booking has been confirmed");
        noti.setMessage(String.format(
                "Your consultation \"%s\" on %s at %s has been confirmed%s.",
                serviceTitle, appointment.getAppointmentDate(),
                appointment.getTimeSlot().toString().substring(0, 5),
                expertName != null ? " — your expert will be " + expertName : ""
        ));
        notificationRepository.save(noti);
    }

    private BookingResponse toResponse(ConsultationAppointment a, double basePrice, double addonsPrice, List<Long> addonIds) {
        BookingResponse r = new BookingResponse();
        r.setId(a.getId());
        r.setServiceId(a.getServiceId());
        r.setClientId(a.getClientId());
        r.setExpertId(a.getExpertId());
        r.setAddonIds(addonIds);
        r.setBasePrice(basePrice);
        r.setAddonsPrice(addonsPrice);
        r.setTotalPrice(a.getTotalPrice());
        r.setAppointmentDate(a.getAppointmentDate().toString());
        r.setTimeSlot(a.getTimeSlot().toString());
        r.setStatus(a.getStatus().name());
        r.setMessageContent(a.getMessageContent());
        r.setAttachmentUrl(a.getAttachmentUrl());
        return r;
    }
}