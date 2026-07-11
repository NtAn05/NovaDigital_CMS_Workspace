package com.example.demo.controller;

import com.example.demo.entity.ProjectAssignment;
import com.example.demo.entity.ProjectAssignment.ProjectRole;
import com.example.demo.entity.ProjectClient;
import com.example.demo.entity.User;
import com.example.demo.repository.ProjectAssignmentRepository;
import com.example.demo.repository.ProjectClientRepository;
import com.example.demo.repository.UserRepository;
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

    /** Returns all projects where the logged-in member is assigned as PM. */
    @GetMapping("/pm-projects")
    public ResponseEntity<?> getMyPmProjects(Authentication authentication) {
        User user = resolveUser(authentication);
        List<ProjectAssignment> assignments =
                assignmentRepository.findByUserIdAndProjectRole(user.getId(), ProjectRole.PM);
        return ResponseEntity.ok(toProjectList(assignments));
    }

    /** Returns all projects where the logged-in member is assigned as STAFF. */
    @GetMapping("/staff-projects")
    public ResponseEntity<?> getMyStaffProjects(Authentication authentication) {
        User user = resolveUser(authentication);
        List<ProjectAssignment> assignments =
                assignmentRepository.findByUserIdAndProjectRole(user.getId(), ProjectRole.STAFF);
        return ResponseEntity.ok(toProjectList(assignments));
    }

    /** Returns all projects the logged-in client has hired. */
    @GetMapping("/client-projects")
    public ResponseEntity<?> getMyClientProjects(Authentication authentication) {
        User user = resolveUser(authentication);
        List<ProjectClient> links = clientRepository.findByUserId(user.getId());

        List<Map<String, Object>> result = links.stream().map(link -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", link.getProject().getId());
            m.put("title", link.getProject().getTitle());
            m.put("description", link.getProject().getDescription());
            m.put("category", link.getProject().getCategory());
            m.put("imageUrl", link.getProject().getImageUrl());
            m.put("technologies", link.getProject().getTechnologies());
            m.put("createdAt", link.getProject().getCreatedAt());
            m.put("hiredAt", link.getHiredAt());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private User resolveUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private List<Map<String, Object>> toProjectList(List<ProjectAssignment> assignments) {
        return assignments.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getProject().getId());
            m.put("title", a.getProject().getTitle());
            m.put("description", a.getProject().getDescription());
            m.put("category", a.getProject().getCategory());
            m.put("imageUrl", a.getProject().getImageUrl());
            m.put("technologies", a.getProject().getTechnologies());
            m.put("createdAt", a.getProject().getCreatedAt());
            m.put("projectRole", a.getProjectRole().name());
            m.put("assignedAt", a.getAssignedAt());
            return m;
        }).collect(Collectors.toList());
    }
}
