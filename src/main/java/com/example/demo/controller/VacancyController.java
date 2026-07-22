package com.example.demo.controller;

import com.example.demo.dto.VacancyResponse;
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

    // ── F_37: Careers Board ────────────────────────────────────────────────────

    /**
     * GET /api/vacancies — Public.
     * Trả Map<workstream, List<VacancyResponse>> để Careers Board render theo nhóm.
     */
    @GetMapping
    public ResponseEntity<Map<String, List<VacancyResponse>>> getVacancies() {
        return ResponseEntity.ok(vacancyService.getActiveVacanciesGroupedByWorkstream());
    }

    /**
     * GET /api/vacancies/list — Public.
     * Trả List phẳng dùng cho dropdown "Chọn vị trí" trong form Apply.
     */
    @GetMapping("/list")
    public ResponseEntity<List<VacancyResponse>> listVacancies() {
        return ResponseEntity.ok(vacancyService.getAllActiveVacancies());
    }

    // ── F_38: Apply ────────────────────────────────────────────────────────────

    /**
     * POST /api/vacancies/apply — Yêu cầu đăng nhập (JWT).
     * Body (JSON): { vacancyId, applicantName, applicantEmail, applicantPhone,
     *               resumeUrl, coverLetter }
     * applicantEmail được overwrite bằng email từ JWT để tránh giả mạo.
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyForVacancy(@RequestBody CandidateApplication application,
                                              Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Override email bằng thông tin từ JWT token (bảo mật)
            if (authentication != null && authentication.getName() != null) {
                application.setApplicantEmail(authentication.getName());
            }
            vacancyService.submitApplication(application);
            response.put("success", true);
            response.put("message", "Nộp hồ sơ thành công! Chúng tôi sẽ liên hệ bạn sớm nhất.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ── HR Dashboard ───────────────────────────────────────────────────────────

    /**
     * GET /api/vacancies/applications — ADMIN/MEMBER only.
     * Optional param: ?vacancyId=X để filter theo vị trí.
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
