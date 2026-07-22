package com.example.demo.controller;

import com.example.demo.entity.ConsultationAppointment;
import com.example.demo.entity.PaymentTransaction;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.AppointmentStatus;
import com.example.demo.repository.*;
import com.example.demo.entity.AppointmentAddon;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PayOS payOS;

    @Autowired
    private ConsultationAppointmentRepository appointmentRepository;

    @Autowired
    private PaymentTransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentAddonRepository appointmentAddonRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ProjectMilestoneRepository milestoneRepository;

    @Autowired
    private ProjectClientRepository projectClientRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @jakarta.annotation.PostConstruct
    public void initDatabaseSchema() {
        try {
            jdbcTemplate.execute("ALTER TABLE payment_transactions ALTER COLUMN appointment_id DROP NOT NULL");
            System.out.println(">>> [PaymentController] Altered payment_transactions table column appointment_id successfully.");
        } catch (Exception e) {
            System.out.println(">>> [PaymentController] Column alteration already done or skipped: " + e.getMessage());
        }
    }

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    @PostMapping("/create-payment-link")
    public ResponseEntity<?> createPaymentLink(@RequestBody Map<String, Long> payload) {
        Long appointmentId = payload.get("appointmentId");
        if (appointmentId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing appointmentId"));
        }

        // 1. Get current authenticated user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in"));
        }

        // 2. Find appointment
        ConsultationAppointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Appointment not found"));
        }

        // 3. Verify ownership (client must match or user must be admin)
        if (!appointment.getClientId().equals(currentUser.getId()) && !currentUser.getRole().equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You are not authorized to pay for this appointment"));
        }

        // 4. Check if already paid / confirmed
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return ResponseEntity.badRequest().body(Map.of("message", "This appointment has already been paid or confirmed"));
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return ResponseEntity.badRequest().body(Map.of("message", "This appointment is cancelled and cannot be paid"));
        }

        // 5. Convert price: USD to VND (1 USD = 25000 VND)
        double priceUsd = appointment.getTotalPrice() != null ? appointment.getTotalPrice() : 0.0;
        long amountVnd = (long) (priceUsd * 25000);
        if (amountVnd < 2000) {
            amountVnd = 2000;
        }

        // 6. Generate unique orderCode
        long orderCode = System.currentTimeMillis();

        // 7. Create PaymentTransaction in DB
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderCode(orderCode);
        transaction.setAppointmentId(appointmentId);
        transaction.setAmount((double) amountVnd);
        transaction.setStatus("PENDING");
        transactionRepository.save(transaction);

        // 8. Call PayOS to create link
        String description = "Payment " + appointmentId;
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amountVnd)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .build();

        try {
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(request);
            Map<String, Object> result = new HashMap<>();
            result.put("checkoutUrl", response.getCheckoutUrl());
            result.put("orderCode", orderCode);
            return ResponseEntity.ok(result);
        } catch (PayOSException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "PayOS payment gateway error: " + e.getMessage()));
        }
    }

    @PostMapping("/create-milestone-payment-link")
    public ResponseEntity<?> createMilestonePaymentLink(@RequestBody Map<String, Long> payload) {
        Long milestoneId = payload.get("milestoneId");
        if (milestoneId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing milestoneId"));
        }

        // 1. Get current authenticated user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in"));
        }

        // 2. Find milestone
        com.example.demo.entity.ProjectMilestone milestone = milestoneRepository.findById(milestoneId).orElse(null);
        if (milestone == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Phase (milestone) not found"));
        }

        // 3. Verify ownership: user must have hired the project (or be Admin)
        Long projectId = milestone.getProject().getId();
        boolean hasHired = projectClientRepository.findByProjectIdAndUserId(projectId, currentUser.getId()).isPresent();
        if (!hasHired && !currentUser.getRole().equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You are not authorized to pay for this project"));
        }

        // 4. Verify phase status is COMPLETED and unpaid
        if (milestone.getStatus() != com.example.demo.entity.enums.MilestoneStatus.COMPLETED) {
            return ResponseEntity.badRequest().body(Map.of("message", "Can only pay for COMPLETED phases"));
        }

        if (Boolean.TRUE.equals(milestone.getPaid())) {
            return ResponseEntity.badRequest().body(Map.of("message", "This phase has already been paid"));
        }

        // 5. Convert price: USD to VND
        double priceUsd = milestone.getPrice() != null ? milestone.getPrice() : 0.0;
        long amountVnd = (long) (priceUsd * 25000);
        if (amountVnd < 2000) {
            amountVnd = 2000;
        }

        // 6. Generate unique orderCode
        long orderCode = System.currentTimeMillis();

        // 7. Create PaymentTransaction in DB
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderCode(orderCode);
        transaction.setMilestoneId(milestoneId);
        transaction.setAmount((double) amountVnd);
        transaction.setStatus("PENDING");
        transactionRepository.save(transaction);

        // 8. Call PayOS to create link
        String description = "Phase payment " + milestoneId;
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amountVnd)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .build();

        try {
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(request);
            Map<String, Object> result = new HashMap<>();
            result.put("checkoutUrl", response.getCheckoutUrl());
            result.put("orderCode", orderCode);
            return ResponseEntity.ok(result);
        } catch (PayOSException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "PayOS payment gateway error: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in"));
        }

        List<Long> appointmentIds = appointmentRepository.findByClientIdOrderByAppointmentDateDesc(currentUser.getId())
                .stream().map(ConsultationAppointment::getId).collect(Collectors.toList());

        List<Long> projectIds = projectClientRepository.findByUserId(currentUser.getId())
                .stream().map(pc -> pc.getProject().getId()).collect(Collectors.toList());
        
        List<Long> milestoneIds = new java.util.ArrayList<>();
        for (Long pid : projectIds) {
            milestoneRepository.findByProjectIdOrderByCreatedAtAsc(pid).forEach(m -> milestoneIds.add(m.getId()));
        }

        List<PaymentTransaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> (t.getAppointmentId() != null && appointmentIds.contains(t.getAppointmentId())) ||
                             (t.getMilestoneId() != null && milestoneIds.contains(t.getMilestoneId())))
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = transactions.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("orderCode", t.getOrderCode());
            m.put("amount", t.getAmount());
            m.put("status", t.getStatus());
            m.put("createdAt", t.getCreatedAt());
            m.put("updatedAt", t.getUpdatedAt());

            if (t.getAppointmentId() != null) {
                m.put("type", "BOOKING");
                m.put("referenceId", t.getAppointmentId());
                ConsultationAppointment appointment = appointmentRepository.findById(t.getAppointmentId()).orElse(null);
                if (appointment != null) {
                    m.put("description", "Consultation booking payment for service #" + appointment.getServiceId());
                } else {
                    m.put("description", "Consultation booking payment");
                }
            } else if (t.getMilestoneId() != null) {
                m.put("type", "MILESTONE");
                m.put("referenceId", t.getMilestoneId());
                com.example.demo.entity.ProjectMilestone milestone = milestoneRepository.findById(t.getMilestoneId()).orElse(null);
                if (milestone != null) {
                    m.put("description", "Payment for phase \"" + milestone.getName() + "\" - Project " + milestone.getProject().getTitle());
                } else {
                    m.put("description", "Project Phase payment");
                }
            }
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/status/{orderCode}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long orderCode) {
        PaymentTransaction transaction = transactionRepository.findByOrderCode(orderCode).orElse(null);
        if (transaction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Transaction not found"));
        }

        if ("PAID".equals(transaction.getStatus())) {
            return ResponseEntity.ok(Map.of("status", "PAID", "appointmentId", 
                transaction.getAppointmentId() != null ? transaction.getAppointmentId() : 0L,
                "milestoneId", transaction.getMilestoneId() != null ? transaction.getMilestoneId() : 0L));
        }

        try {
            PaymentLink paymentLinkInfo = payOS.paymentRequests().get(orderCode);
            PaymentLinkStatus status = paymentLinkInfo.getStatus();
            
            if (PaymentLinkStatus.PAID == status) {
                confirmPaymentInDb(transaction);
                return ResponseEntity.ok(Map.of("status", "PAID", 
                    "appointmentId", transaction.getAppointmentId() != null ? transaction.getAppointmentId() : 0L,
                    "milestoneId", transaction.getMilestoneId() != null ? transaction.getMilestoneId() : 0L));
            } else if (PaymentLinkStatus.CANCELLED == status) {
                transaction.setStatus("CANCELLED");
                transactionRepository.save(transaction);
                return ResponseEntity.ok(Map.of("status", "CANCELLED"));
            } else {
                return ResponseEntity.ok(Map.of("status", status.name()));
            }
        } catch (PayOSException e) {
            return ResponseEntity.ok(Map.of("status", transaction.getStatus(), "message", "Unable to update from PayOS: " + e.getMessage()));
        }
    }

    @PostMapping("/payos-webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody Webhook body) {
        try {
            WebhookData data = payOS.webhooks().verify(body);
            Long orderCode = data.getOrderCode();
            
            PaymentTransaction transaction = transactionRepository.findByOrderCode(orderCode).orElse(null);
            if (transaction != null && !"PAID".equals(transaction.getStatus())) {
                confirmPaymentInDb(transaction);
            }
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid signature: " + e.getMessage()));
        }
    }

    private void confirmPaymentInDb(PaymentTransaction transaction) {
        System.out.println(">>> [PaymentController] confirmPaymentInDb called for transaction orderCode: " + transaction.getOrderCode());
        System.out.println(">>> [PaymentController] appointmentId: " + transaction.getAppointmentId() + ", milestoneId: " + transaction.getMilestoneId());

        transaction.setStatus("PAID");
        transactionRepository.save(transaction);

        if (transaction.getAppointmentId() != null && transaction.getAppointmentId() > 0) {
            System.out.println(">>> [PaymentController] Processing booking payment for appointmentId: " + transaction.getAppointmentId());
            ConsultationAppointment appointment = appointmentRepository.findById(transaction.getAppointmentId()).orElse(null);
            if (appointment != null && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
                AppointmentStatus statusBefore = appointment.getStatus();
                appointment.setStatus(AppointmentStatus.CONFIRMED);
                appointmentRepository.save(appointment);

                // Send notifications
                notifyParties(appointment, statusBefore);
            }
        }
        
        if (transaction.getMilestoneId() != null && transaction.getMilestoneId() > 0) {
            System.out.println(">>> [PaymentController] Processing milestone payment for milestoneId: " + transaction.getMilestoneId());
            com.example.demo.entity.ProjectMilestone milestone = milestoneRepository.findById(transaction.getMilestoneId()).orElse(null);
            if (milestone != null) {
                System.out.println(">>> [PaymentController] Found milestone: " + milestone.getName() + ", paid before: " + milestone.getPaid());
                if (!Boolean.TRUE.equals(milestone.getPaid())) {
                    milestone.setPaid(true);
                    milestoneRepository.save(milestone);
                    System.out.println(">>> [PaymentController] Updated milestone paid to true and saved successfully.");

                    // Send notifications
                    notifyMilestonePaid(milestone);
                }
            } else {
                System.out.println(">>> [PaymentController] Milestone NOT found for ID: " + transaction.getMilestoneId());
            }
        }
    }

    private void notifyMilestonePaid(com.example.demo.entity.ProjectMilestone milestone) {
        List<com.example.demo.entity.ProjectClient> clients = projectClientRepository.findByProjectId(milestone.getProject().getId());
        String milestoneName = milestone.getName();
        String projectTitle = milestone.getProject().getTitle();

        // Notify client
        for (com.example.demo.entity.ProjectClient pc : clients) {
            Notification noti = new Notification();
            noti.setUserId(pc.getUser().getId());
            noti.setTitle("Phase Payment Confirmed");
            noti.setMessage(String.format("Your payment for phase \"%s\" of project \"%s\" has been received and confirmed.", milestoneName, projectTitle));
            noti.setLink("transaction.html");
            notificationRepository.save(noti);
        }

        // Notify PM
        List<com.example.demo.entity.ProjectAssignment> assignments = projectAssignmentRepository.findByProjectId(milestone.getProject().getId());
        for (com.example.demo.entity.ProjectAssignment pa : assignments) {
            if (pa.getProjectRole() == com.example.demo.entity.ProjectAssignment.ProjectRole.PM) {
                Notification noti = new Notification();
                noti.setUserId(pa.getUser().getId());
                noti.setTitle("Client Paid for Milestone");
                noti.setMessage(String.format("Client has paid for completed phase \"%s\" of project \"%s\".", milestoneName, projectTitle));
                noti.setLink("pm-dashboard.html");
                notificationRepository.save(noti);
            }
        }
    }

    private void notifyParties(ConsultationAppointment appointment, AppointmentStatus statusBefore) {
        boolean justConfirmed = appointment.getStatus() == AppointmentStatus.CONFIRMED
                && statusBefore != AppointmentStatus.CONFIRMED;
        if (justConfirmed) {
            if (appointment.getExpertId() != null) {
                notifyExpertBookingConfirmed(appointment);
            }
            notifyClientBookingConfirmed(appointment);
        }
    }

    private void notifyExpertBookingConfirmed(ConsultationAppointment appointment) {
        Long expertUserId = appointment.getExpertId();
        if (expertUserId == null) return;

        String customerName = userRepository.findById(appointment.getClientId())
                .map(User::getFullName)
                .orElse("Customer #" + appointment.getClientId());

        String serviceTitle = serviceRepository.findById(appointment.getServiceId())
                .map(Service::getTitle)
                .orElse("service #" + appointment.getServiceId());

        Notification noti = new Notification();
        noti.setUserId(expertUserId);
        noti.setTitle("You've been assigned a new consultation booking (Paid)");
        noti.setMessage(String.format(
                "Customer %s's consultation \"%s\" has been paid and confirmed for %s at %s.",
                customerName, serviceTitle, appointment.getAppointmentDate(),
                appointment.getTimeSlot().toString().substring(0, 5)
        ));
        noti.setLink("member-contact.html#my-bookings");
        notificationRepository.save(noti);
    }

    private void notifyClientBookingConfirmed(ConsultationAppointment appointment) {
        Long clientUserId = appointment.getClientId();
        if (clientUserId == null) return;

        String serviceTitle = serviceRepository.findById(appointment.getServiceId())
                .map(Service::getTitle)
                .orElse("service #" + appointment.getServiceId());

        String expertName = appointment.getExpertId() != null
                ? userRepository.findById(appointment.getExpertId())
                        .map(User::getFullName)
                        .orElse(null)
                : null;

        Notification noti = new Notification();
        noti.setUserId(clientUserId);
        noti.setTitle("Your consultation payment has been confirmed");
        noti.setMessage(String.format(
                "Your payment for consultation \"%s\" on %s at %s has been received and confirmed%s.",
                serviceTitle, appointment.getAppointmentDate(),
                appointment.getTimeSlot().toString().substring(0, 5),
                expertName != null ? " — your expert will be " + expertName : ""
        ));
        notificationRepository.save(noti);
    }
}
