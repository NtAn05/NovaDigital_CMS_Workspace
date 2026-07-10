package com.example.demo.controller;

import com.example.demo.dto.MilestoneBroadcastPayload;
import com.example.demo.dto.MilestoneCreateRequest;
import com.example.demo.dto.MilestoneResponse;
import com.example.demo.dto.MilestoneSyncRequest;
import com.example.demo.entity.MilestoneMutationLog;
import com.example.demo.service.MilestoneService;
import com.example.demo.service.SseBroadcastService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for the Project Milestone Sync Node (UC-12).
 *
 * Base URL: /api/projects/{projectId}/milestones
 * SSE stream: /api/milestones/stream
 *
 * Endpoint summary:
 *   GET    /api/projects/{projectId}/milestones              → List all milestones for a project
 *   POST   /api/projects/{projectId}/milestones              → Create a new milestone
 *   PUT    /api/projects/{projectId}/milestones/{id}/sync    → Sync progress/status (CORE)
 *   DELETE /api/projects/{projectId}/milestones/{id}         → Delete a milestone
 *   GET    /api/projects/{projectId}/milestones/{id}/logs    → Audit trail for a milestone
 *   GET    /api/milestones/stream                            → SSE broadcast stream
 */
@RestController
public class ProjectMilestoneController {

    @Autowired
    private MilestoneService milestoneService;

    @Autowired
    private SseBroadcastService sseBroadcastService;

    // ── GET ALL ──────────────────────────────────────────────────────────────

    /**
     * Returns all milestones for a project.
     * Public endpoint — clients can view project progress without authentication.
     */
    @GetMapping("/api/projects/{projectId}/milestones")
    public ResponseEntity<?> getMilestones(@PathVariable Long projectId) {
        try {
            List<MilestoneResponse> milestones = milestoneService.getMilestonesByProject(projectId);
            return ResponseEntity.ok(milestones);
        } catch (EntityNotFoundException e) {
            return notFound(e.getMessage());
        }
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Creates a new milestone for a project.
     * Requires ADMIN or MEMBER role (enforced in SecurityConfig).
     *
     * @Valid triggers Bean Validation on the request body.
     * Authentication is injected by Spring Security to extract the performer's username.
     */
    @PostMapping("/api/projects/{projectId}/milestones")
    public ResponseEntity<?> createMilestone(@PathVariable Long projectId,
                                             @Valid @RequestBody MilestoneCreateRequest request,
                                             Authentication authentication) {
        try {
            String performedBy = authentication.getName();
            MilestoneResponse created = milestoneService.createMilestone(projectId, request, performedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (EntityNotFoundException e) {
            return notFound(e.getMessage());
        } catch (AccessDeniedException e) {
            return forbidden(e.getMessage());
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    // ── SYNC (Core UC-12 Endpoint) ────────────────────────────────────────────

    /**
     * ★ The Sync Endpoint — Core of UC-12 ★
     *
     * Triggered when PM updates a milestone's progress or status on Admin UI.
     * A single call to this endpoint atomically:
     *   1. Validates input (0 ≤ progressPercentage ≤ 100, status not null)
     *   2. Updates the milestone record in DB (@Transactional)
     *   3. Writes granular Mutation Logs (one per changed field)
     *   4. Instantly broadcasts the new state to all Layout Nodes via SSE (@Async)
     *
     * Requires ADMIN or MEMBER role.
     */
    @PutMapping("/api/projects/{projectId}/milestones/{id}/sync")
    public ResponseEntity<?> syncMilestone(@PathVariable Long projectId,
                                           @PathVariable Long id,
                                           @Valid @RequestBody MilestoneSyncRequest request,
                                           Authentication authentication) {
        try {
            String performedBy = authentication.getName();
            MilestoneResponse updated = milestoneService.syncMilestone(projectId, id, request, performedBy);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return notFound(e.getMessage());
        } catch (AccessDeniedException e) {
            return forbidden(e.getMessage());
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * Deletes a milestone by ID.
     * Also writes a DELETE mutation log and broadcasts removal to Layout Nodes.
     * Requires ADMIN role only (enforced in SecurityConfig).
     */
    @DeleteMapping("/api/projects/{projectId}/milestones/{id}")
    public ResponseEntity<?> deleteMilestone(@PathVariable Long projectId,
                                             @PathVariable Long id,
                                             Authentication authentication) {
        try {
            String performedBy = authentication.getName();
            milestoneService.deleteMilestone(projectId, id, performedBy);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Milestone deleted successfully.");
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return notFound(e.getMessage());
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    // ── AUDIT LOGS ────────────────────────────────────────────────────────────

    /**
     * Returns the full audit trail (mutation history) for a specific milestone.
     * Requires ADMIN or MEMBER role.
     */
    @GetMapping("/api/projects/{projectId}/milestones/{id}/logs")
    public ResponseEntity<?> getMutationLogs(@PathVariable Long projectId,
                                             @PathVariable Long id) {
        try {
            List<MilestoneMutationLog> logs = milestoneService.getMutationLogs(id);
            return ResponseEntity.ok(logs);
        } catch (EntityNotFoundException e) {
            return notFound(e.getMessage());
        }
    }

    // ── SSE STREAM ────────────────────────────────────────────────────────────

    /**
     * Server-Sent Events streaming endpoint.
     * Layout Nodes (Client View, Dashboard widgets) connect here to receive
     * real-time milestone updates without polling or page reloads.
     *
     * Frontend usage:
     * <pre>
     *   const es = new EventSource('/api/milestones/stream');
     *   es.addEventListener('milestone-update', (e) => {
     *     const payload = JSON.parse(e.data);
     *     // Filter by projectId if needed:
     *     if (payload.projectId === currentProjectId) {
     *       updateMilestoneUI(payload.milestone);
     *       showToast(payload.mutationSummary);
     *     }
     *   });
     * </pre>
     *
     * Produces text/event-stream. Public endpoint — no auth required for viewing
     * live status, since milestone progress is visible on the public Client View.
     */
    @GetMapping(value = "/api/milestones/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMilestoneUpdates() {
        return sseBroadcastService.subscribe();
    }

    // ── Private Error Helpers ─────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> notFound(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Not Found");
        body.put("message", message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    private ResponseEntity<Map<String, Object>> forbidden(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Forbidden");
        body.put("message", message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Bad Request");
        body.put("message", message);
        return ResponseEntity.badRequest().body(body);
    }
}
