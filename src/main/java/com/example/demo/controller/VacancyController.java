package com.example.demo.controller;

import com.example.demo.entity.CandidateApplication;
import com.example.demo.entity.enums.ApplicationStatus;
import com.example.demo.service.VacancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyController {

    private final VacancyService vacancyService;

    // ── F_38: Apply ────────────────────────────────────────────────────────────

    /**
     * POST /api/vacancies/apply — Requires authentication (JWT).
     * Body (JSON): { vacancyId, applicantName, applicantEmail, applicantPhone,
     * resumeUrl, coverLetter }
     * applicantEmail is overwritten with email from JWT to prevent spoofing.
     * Status is automatically set to PENDING by the entity default.
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyForVacancy(@RequestBody CandidateApplication application,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Override email with info from JWT token (security)
            if (authentication != null && authentication.getName() != null) {
                application.setApplicantEmail(authentication.getName());
            }
            vacancyService.submitApplication(application);
            response.put("success", true);
            response.put("message", "Application submitted successfully! We will contact you as soon as possible.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ── HR Dashboard: Read ─────────────────────────────────────────────────────

    /**
     * GET /api/vacancies/applications — ADMIN/MEMBER/RESOURCE only.
     * Optional params:
     *   ?vacancyId=X   → filter by vacancy
     *   ?status=PENDING → filter by pipeline status
     *   Both can be combined.
     */
    @GetMapping("/applications")
    public ResponseEntity<List<CandidateApplication>> getApplications(
            @RequestParam(required = false) Long vacancyId,
            @RequestParam(required = false) String status) {

        ApplicationStatus appStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                appStatus = ApplicationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Unknown status → treat as no filter
            }
        }

        List<CandidateApplication> result;
        if (vacancyId != null && appStatus != null) {
            result = vacancyService.getApplicationsByVacancyAndStatus(vacancyId, appStatus);
        } else if (vacancyId != null) {
            result = vacancyService.getApplicationsByVacancy(vacancyId);
        } else if (appStatus != null) {
            result = vacancyService.getApplicationsByStatus(appStatus);
        } else {
            result = vacancyService.getAllApplications();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/vacancies/applications/my — Fetches applications for the logged-in candidate
     */
    @GetMapping("/applications/my")
    public ResponseEntity<?> getMyApplications(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        String email = authentication.getName();
        List<CandidateApplication> result = vacancyService.getApplicationsByEmail(email);
        return ResponseEntity.ok(result);
    }

    /**
     * PUT /api/vacancies/applications/my/{id} — Candidate updates their own application
     */
    @PutMapping("/applications/my/{id}")
    public ResponseEntity<?> updateMyApplication(
            @PathVariable Long id,
            @RequestBody CandidateApplication updatedData,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        try {
            CandidateApplication saved = vacancyService.updateMyApplication(id, authentication.getName(), updatedData);
            return ResponseEntity.ok(Map.of("success", true, "message", "Application updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * DELETE /api/vacancies/applications/my/{id} — Candidate deletes their own application
     */
    @DeleteMapping("/applications/my/{id}")
    public ResponseEntity<?> deleteMyApplication(
            @PathVariable Long id,
            Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        try {
            vacancyService.deleteMyApplication(id, authentication.getName());
            return ResponseEntity.ok(Map.of("success", true, "message", "Application withdrawn successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ── HR Dashboard: Status Pipeline ──────────────────────────────────────────

    /**
     * PATCH /api/vacancies/applications/{id}/status — ADMIN/MEMBER/RESOURCE only.
     * Body (JSON): { "status": "INTERVIEW" }
     * Transitions an application through the recruitment pipeline:
     *   PENDING → INTERVIEW → APPROVED | REJECTED
     */
    @PatchMapping("/applications/{id}/status")
    public ResponseEntity<?> updateApplicationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String rawStatus = body.get("status");
            if (rawStatus == null || rawStatus.isBlank()) {
                response.put("success", false);
                response.put("message", "Missing 'status' field in request body.");
                return ResponseEntity.badRequest().body(response);
            }
            ApplicationStatus newStatus = ApplicationStatus.valueOf(rawStatus.toUpperCase());
            CandidateApplication updated = vacancyService.updateApplicationStatus(id, newStatus);
            response.put("success", true);
            response.put("message", "Status updated to " + newStatus);
            response.put("id", updated.getId());
            response.put("status", updated.getStatus().name());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Invalid status value. Allowed: PENDING, VIEWED, INTERVIEW, APPROVED, REJECTED");
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
