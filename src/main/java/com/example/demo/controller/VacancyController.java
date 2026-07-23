package com.example.demo.controller;

import com.example.demo.entity.CandidateApplication;
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

<<<<<<< Updated upstream
    // ── F_37: Careers Board ────────────────────────────────────────────────────

    /**
     * GET /api/vacancies — Public.
     * Returns Map<workstream, List<VacancyResponse>> for Careers Board grouped rendering.
     */
    @GetMapping
    public ResponseEntity<Map<String, List<VacancyResponse>>> getVacancies() {
        return ResponseEntity.ok(vacancyService.getActiveVacanciesGroupedByWorkstream());
    }

    /**
     * GET /api/vacancies/list — Public.
     * Returns flat List used for "Select position" dropdown in Apply form.
     */
    @GetMapping("/list")
    public ResponseEntity<List<VacancyResponse>> listVacancies() {
        return ResponseEntity.ok(vacancyService.getAllActiveVacancies());
    }

=======
>>>>>>> Stashed changes
    // ── F_38: Apply ────────────────────────────────────────────────────────────

    /**
     * POST /api/vacancies/apply — Requires authentication (JWT).
     * Body (JSON): { vacancyId, applicantName, applicantEmail, applicantPhone,
     *               resumeUrl, coverLetter }
     * applicantEmail is overwritten with email from JWT to prevent spoofing.
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

    // ── HR Dashboard ───────────────────────────────────────────────────────────

    /**
     * GET /api/vacancies/applications — ADMIN/MEMBER only.
     * Optional param: ?vacancyId=X to filter by position.
     */
    @GetMapping("/applications")
    public ResponseEntity<List<CandidateApplication>> getApplications(
            @RequestParam(required = false) Long vacancyId) {
        if (vacancyId != null) {
            return ResponseEntity.ok(vacancyService.getApplicationsByVacancy(vacancyId));
        }
        return ResponseEntity.ok(vacancyService.getAllApplications());
    }
}
