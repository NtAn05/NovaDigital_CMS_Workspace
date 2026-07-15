package com.example.demo.controller;

import com.example.demo.entity.ConsultationAppointment;
import com.example.demo.entity.enums.AppointmentStatus;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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

    @GetMapping
    public ResponseEntity<?> getDashboardStats() {
        long messagesCount = contactRepository.count();
        long accountsCount = userRepository.count();
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

        for (ConsultationAppointment app : appointments) {
            if (app.getStatus() != AppointmentStatus.CANCELLED && app.getAppointmentDate() != null) {
                LocalDate date = app.getAppointmentDate();
                String key = String.format("%d-%02d", date.getYear(), date.getMonthValue());
                double price = app.getTotalPrice() != null ? app.getTotalPrice() : 0.0;
                monthlyRevenue.merge(key, price, Double::sum);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("messagesCount", messagesCount);
        response.put("accountsCount", accountsCount);
        response.put("membersCount", membersCount);
        response.put("projectsCount", projectsCount);
        response.put("servicesCount", servicesCount);
        response.put("appointmentsCount", appointmentsCount);
        response.put("revenueData", monthlyRevenue);

        return ResponseEntity.ok(response);
    }
}
