package com.example.demo.controller;

import com.example.demo.entity.ProjectAssignment;
import com.example.demo.entity.ProjectAssignment.ProjectRole;
import com.example.demo.entity.ProjectClient;
import com.example.demo.entity.User;
import com.example.demo.entity.ConsultationAppointment;
import com.example.demo.entity.Service;
import com.example.demo.entity.AppointmentAddon;
import com.example.demo.entity.ServiceAddon;
import com.example.demo.repository.AppointmentAddonRepository;
import com.example.demo.repository.ServiceAddonRepository;
import com.example.demo.repository.ProjectAssignmentRepository;
import com.example.demo.repository.ProjectClientRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ConsultationAppointmentRepository;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides "my projects" endpoints scoped to the currently authenticated user.
 *
 * GET /api/my/pm-projects    → Projects where I am PM (ROLE_MEMBER)
 * GET /api/my/staff-projects → Projects where I am STAFF (ROLE_MEMBER)
 * GET /api/my/client-projects → Projects I have hired (ROLE_USER)
 *
 * All routes require authentication (enforced in SecurityConfig).
 */
@RestController
@RequestMapping("/api/my")
public class MyProjectsController {

    @Autowired
    private ProjectAssignmentRepository assignmentRepository;

    @Autowired
    private ProjectClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsultationAppointmentRepository appointmentRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private AppointmentAddonRepository appointmentAddonRepository;

    @Autowired
    private ServiceAddonRepository serviceAddonRepository;

    /**
     * Returns consultation bookings assigned to the logged-in member
     * (expertId = User.id of current user, this user has role ROLE_MEMBER), including real customer name.
     * Used for the "My Consultation Bookings" section on PM/member dashboard.
     *
     * Note: expert_id points directly to User.id (role ROLE_MEMBER), NOT members table -
     * members table does not participate in system authorization/login.
     */
    @GetMapping("/bookings")
    public ResponseEntity<?> getMyBookings(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthenticated or user not found"));
        }

        List<ConsultationAppointment> appointments =
                appointmentRepository.findByExpertIdOrderByAppointmentDateDesc(user.getId());

        List<Map<String, Object>> result = appointments.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("status", a.getStatus() != null ? a.getStatus().name() : "PENDING");
            m.put("appointmentDate", a.getAppointmentDate());
            m.put("timeSlot", a.getTimeSlot());
            m.put("messageContent", a.getMessageContent());
            m.put("attachmentUrl", a.getAttachmentUrl());
            m.put("totalPrice", a.getTotalPrice() != null ? a.getTotalPrice() : 0.0);

            // Real customer name + email, fetched from User via clientId
            User client = a.getClientId() != null ? userRepository.findById(a.getClientId()).orElse(null) : null;
            m.put("customerName", client != null ? client.getFullName() : (a.getClientId() != null ? "Customer #" + a.getClientId() : "Unknown"));
            m.put("customerEmail", client != null ? client.getEmail() : "");
            m.put("customerPhone", client != null ? client.getPhone() : "");

            // Service name and base price
            Service service = a.getServiceId() != null ? serviceRepository.findById(a.getServiceId()).orElse(null) : null;
            m.put("serviceTitle", service != null ? service.getTitle() : (a.getServiceId() != null ? "Service #" + a.getServiceId() : "N/A"));
            double basePrice = a.getBasePrice() != null ? a.getBasePrice() : (service != null && service.getBasePrice() != null ? service.getBasePrice() : 0.0);
            m.put("basePrice", basePrice);

            // Add-ons list and total add-ons price
            List<AppointmentAddon> apptAddons = a.getId() != null ? appointmentAddonRepository.findByAppointmentId(a.getId()) : java.util.Collections.emptyList();
            List<Map<String, Object>> addonsList = apptAddons.stream().map(aa -> {
                ServiceAddon sa = (aa != null && aa.getAddonId() != null) ? serviceAddonRepository.findById(aa.getAddonId()).orElse(null) : null;
                Map<String, Object> addonMap = new HashMap<>();
                addonMap.put("id", aa != null ? aa.getAddonId() : null);
                addonMap.put("addonName", sa != null ? sa.getAddonName() : (aa != null && aa.getAddonId() != null ? "Add-on #" + aa.getAddonId() : "Unknown Add-on"));
                addonMap.put("priceModifier", sa != null && sa.getPriceModifier() != null ? sa.getPriceModifier() : 0.0);
                return addonMap;
            }).collect(Collectors.toList());

            double addonsPrice = addonsList.stream().mapToDouble(am -> {
                Object pm = am.get("priceModifier");
                return pm instanceof Number ? ((Number) pm).doubleValue() : 0.0;
            }).sum();
            m.put("addonsPrice", addonsPrice);
            m.put("addons", addonsList);

            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /** Returns all projects where the logged-in member is assigned as PM. */
    @GetMapping("/pm-projects")
    public ResponseEntity<?> getMyPmProjects(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthenticated or user not found"));
        }
        List<ProjectAssignment> assignments =
                assignmentRepository.findByUserIdAndProjectRole(user.getId(), ProjectRole.PM);
        return ResponseEntity.ok(toProjectList(assignments));
    }

    /** Returns all projects where the logged-in member is assigned as STAFF. */
    @GetMapping("/staff-projects")
    public ResponseEntity<?> getMyStaffProjects(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthenticated or user not found"));
        }
        List<ProjectAssignment> assignments =
                assignmentRepository.findByUserIdAndProjectRole(user.getId(), ProjectRole.STAFF);
        return ResponseEntity.ok(toProjectList(assignments));
    }

    /** Returns all projects the logged-in client has hired. */
    @GetMapping("/client-projects")
    public ResponseEntity<?> getMyClientProjects(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthenticated or user not found"));
        }
        List<ProjectClient> links = clientRepository.findByUserId(user.getId());

        List<Map<String, Object>> result = links.stream().map(link -> {
            Map<String, Object> m = new HashMap<>();
            if (link.getProject() != null) {
                m.put("id", link.getProject().getId());
                m.put("title", link.getProject().getTitle());
                m.put("description", link.getProject().getDescription());
                m.put("category", link.getProject().getCategory());
                m.put("imageUrl", link.getProject().getImageUrl());
                m.put("technologies", link.getProject().getTechnologies());
                m.put("createdAt", link.getProject().getCreatedAt());
            }
            m.put("hiredAt", link.getHiredAt());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private User resolveUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsernameOrEmail(username, username).orElse(null);
    }

    private List<Map<String, Object>> toProjectList(List<ProjectAssignment> assignments) {
        return assignments.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            if (a.getProject() != null) {
                m.put("id", a.getProject().getId());
                m.put("title", a.getProject().getTitle());
                m.put("description", a.getProject().getDescription());
                m.put("category", a.getProject().getCategory());
                m.put("imageUrl", a.getProject().getImageUrl());
                m.put("technologies", a.getProject().getTechnologies());
                m.put("createdAt", a.getProject().getCreatedAt());
            }
            if (a.getProjectRole() != null) {
                m.put("projectRole", a.getProjectRole().name());
            }
            m.put("assignedAt", a.getAssignedAt());
            return m;
        }).collect(Collectors.toList());
    }
}