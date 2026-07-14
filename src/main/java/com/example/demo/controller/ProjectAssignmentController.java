package com.example.demo.controller;

import com.example.demo.entity.Project;
import com.example.demo.entity.ProjectAssignment;
import com.example.demo.entity.ProjectAssignment.ProjectRole;
import com.example.demo.entity.ProjectClient;
import com.example.demo.entity.User;
import com.example.demo.repository.ProjectAssignmentRepository;
import com.example.demo.repository.ProjectClientRepository;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin-only controller for managing project assignments (PM/Staff)
 * and client-project linkages.
 *
 * All endpoints require ROLE_ADMIN (enforced in SecurityConfig).
 *
 * Assignments:
 *   GET    /api/projects/{id}/assignments              → list members on a project
 *   POST   /api/projects/{id}/assignments              → assign a member (PM or STAFF)
 *   PUT    /api/projects/{id}/assignments/{userId}     → change member's role on project
 *   DELETE /api/projects/{id}/assignments/{userId}     → remove member from project
 *
 * Clients:
 *   GET    /api/projects/{id}/clients                 → list clients of a project
 *   POST   /api/projects/{id}/clients                 → link a client to a project
 *   DELETE /api/projects/{id}/clients/{userId}        → unlink a client from a project
 */
@RestController
public class ProjectAssignmentController {

    @Autowired
    private ProjectAssignmentRepository assignmentRepository;

    @Autowired
    private ProjectClientRepository clientRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // ASSIGNMENTS
    // ─────────────────────────────────────────────────────────────────────────

    /** List all member assignments for a project. */
    @GetMapping("/api/projects/{projectId}/assignments")
    public ResponseEntity<?> listAssignments(@PathVariable Long projectId) {
        try {
            ensureProjectExists(projectId);
            List<ProjectAssignment> assignments = assignmentRepository.findByProjectId(projectId);
            List<Map<String, Object>> result = assignments.stream().map(a -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", a.getId());
                m.put("userId", a.getUser().getId());
                m.put("username", a.getUser().getUsername());
                m.put("fullName", a.getUser().getFullName());
                m.put("email", a.getUser().getEmail());
                m.put("avatarUrl", a.getUser().getAvatarUrl());
                m.put("projectRole", a.getProjectRole().name());
                m.put("assignedAt", a.getAssignedAt());
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return notFound(e.getMessage());
        }
    }

    /** Assign a member (ROLE_MEMBER) to a project with PM or STAFF role. */
    @PostMapping("/api/projects/{projectId}/assignments")
    public ResponseEntity<?> assignMember(@PathVariable Long projectId,
                                          @RequestBody Map<String, String> body) {
        try {
            Project project = findProjectOrThrow(projectId);
            Long userId = Long.parseLong(body.get("userId"));
            String roleStr = body.getOrDefault("projectRole", "STAFF").toUpperCase();

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

            if (!user.getRole().equals("ROLE_MEMBER")) {
                return badRequest("Only users with ROLE_MEMBER can be assigned as PM or STAFF.");
            }

            // If already assigned, update the role instead of creating a duplicate
            ProjectAssignment assignment = assignmentRepository
                    .findByProjectIdAndUserId(projectId, userId)
                    .orElse(new ProjectAssignment());

            assignment.setProject(project);
            assignment.setUser(user);
            assignment.setProjectRole(ProjectRole.valueOf(roleStr));
            assignmentRepository.save(assignment);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", user.getFullName() + " assigned as " + roleStr + " on project " + project.getTitle());
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return notFound(e.getMessage());
        } catch (IllegalArgumentException e) {
            return badRequest("Invalid role. Use PM or STAFF.");
        }
    }

    /** Change a member's role on a project (PM ↔ STAFF). */
    @PutMapping("/api/projects/{projectId}/assignments/{userId}")
    public ResponseEntity<?> updateRole(@PathVariable Long projectId,
                                        @PathVariable Long userId,
                                        @RequestBody Map<String, String> body) {
        try {
            ensureProjectExists(projectId);
            String roleStr = body.getOrDefault("projectRole", "STAFF").toUpperCase();
            ProjectAssignment assignment = assignmentRepository
                    .findByProjectIdAndUserId(projectId, userId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No assignment found for user " + userId + " on project " + projectId));

            assignment.setProjectRole(ProjectRole.valueOf(roleStr));
            assignmentRepository.save(assignment);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Role updated to " + roleStr);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return notFound(e.getMessage());
        } catch (IllegalArgumentException e) {
            return badRequest("Invalid role. Use PM or STAFF.");
        }
    }

    /** Remove a member from a project. */
    @DeleteMapping("/api/projects/{projectId}/assignments/{userId}")
    @Transactional
    public ResponseEntity<?> removeAssignment(@PathVariable Long projectId,
                                              @PathVariable Long userId) {
        try {
            ensureProjectExists(projectId);
            assignmentRepository.deleteByProjectIdAndUserId(projectId, userId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Member removed from project.");
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return notFound(e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CLIENTS
    // ─────────────────────────────────────────────────────────────────────────

    /** List all clients linked to a project. */
    @GetMapping("/api/projects/{projectId}/clients")
    public ResponseEntity<?> listClients(@PathVariable Long projectId) {
        try {
            ensureProjectExists(projectId);
            List<ProjectClient> clients = clientRepository.findByProjectId(projectId);
            List<Map<String, Object>> result = clients.stream().map(c -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", c.getId());
                m.put("userId", c.getUser().getId());
                m.put("username", c.getUser().getUsername());
                m.put("fullName", c.getUser().getFullName());
                m.put("email", c.getUser().getEmail());
                m.put("hiredAt", c.getHiredAt());
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return notFound(e.getMessage());
        }
    }

    /** Link a client user (ROLE_USER) to a project. */
    @PostMapping("/api/projects/{projectId}/clients")
    public ResponseEntity<?> addClient(@PathVariable Long projectId,
                                       @RequestBody Map<String, String> body) {
        try {
            Project project = findProjectOrThrow(projectId);
            Long userId = Long.parseLong(body.get("userId"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

            if (!user.getRole().equals("ROLE_USER")) {
                return badRequest("Only users with ROLE_USER (clients) can be linked as project clients.");
            }

            // Idempotent — ignore if already linked
            if (clientRepository.findByProjectIdAndUserId(projectId, userId).isEmpty()) {
                ProjectClient link = new ProjectClient();
                link.setProject(project);
                link.setUser(user);
                clientRepository.save(link);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", user.getFullName() + " linked as client to project " + project.getTitle());
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return notFound(e.getMessage());
        }
    }

    /** Unlink a client from a project. */
    @DeleteMapping("/api/projects/{projectId}/clients/{userId}")
    @Transactional
    public ResponseEntity<?> removeClient(@PathVariable Long projectId,
                                          @PathVariable Long userId) {
        try {
            ensureProjectExists(projectId);
            clientRepository.deleteByProjectIdAndUserId(projectId, userId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Client unlinked from project.");
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return notFound(e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));
    }

    private void ensureProjectExists(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException("Project not found: " + projectId);
        }
    }

    private ResponseEntity<Map<String, Object>> notFound(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Not Found");
        body.put("message", message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Bad Request");
        body.put("message", message);
        return ResponseEntity.badRequest().body(body);
    }
}
