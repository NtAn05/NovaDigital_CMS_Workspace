package com.example.demo.controller;

import com.example.demo.entity.ConsultationAppointment;
import com.example.demo.entity.PaymentTransaction;
import com.example.demo.entity.enums.AppointmentStatus;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/admin/dashboard-stats")
public class AdminDashboardController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ConsultationAppointmentRepository appointmentRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @GetMapping
    public ResponseEntity<?> getDashboardStats() {
        long messagesCount = contactRepository.count();
        // Only count users with ROLE_USER
        long accountsCount = userRepository.findAll().stream()
                .filter(u -> "ROLE_USER".equals(u.getRole()))
                .count();
        long membersCount = memberRepository.count();
        long projectsCount = projectRepository.count();
        long servicesCount = serviceRepository.count();

        List<ConsultationAppointment> appointments = appointmentRepository.findAll();
        long appointmentsCount = appointments.size();

        Map<String, Double> monthlyRevenue = new TreeMap<>();
        LocalDate now = LocalDate.now();
        // prefill last 6 months with 0.0
        for (int i = 5; i >= 0; i--) {
            LocalDate target = now.minusMonths(i);
            String key = String.format("%d-%02d", target.getYear(), target.getMonthValue());
            monthlyRevenue.put(key, 0.0);
        }

        List<PaymentTransaction> transactions = paymentTransactionRepository.findAll();
        for (PaymentTransaction tx : transactions) {
            if ("PAID".equalsIgnoreCase(tx.getStatus()) && tx.getCreatedAt() != null) {
                LocalDateTime ldt = tx.getCreatedAt();
                String key = String.format("%d-%02d", ldt.getYear(), ldt.getMonthValue());
                double amount = tx.getAmount() != null ? tx.getAmount() : 0.0;
                monthlyRevenue.merge(key, amount, Double::sum);
            }
        }

        long transactionsCount = transactions.stream()
                .filter(tx -> "PAID".equalsIgnoreCase(tx.getStatus()))
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("messagesCount", messagesCount);
        response.put("accountsCount", accountsCount);
        response.put("membersCount", membersCount);
        response.put("projectsCount", projectsCount);
        response.put("servicesCount", servicesCount);
        response.put("appointmentsCount", appointmentsCount);
        response.put("transactionsCount", transactionsCount);
        response.put("revenueData", monthlyRevenue);

        return ResponseEntity.ok(response);
    }
}
