package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * F_38 — Lưu thông tin ứng viên đã nộp hồ sơ cho một vị trí tuyển dụng.
 * Không tạo FK cứng vào bảng users vì ứng viên có thể không có tài khoản.
 */
@Entity
@Table(name = "candidate_applications")
@Data
@NoArgsConstructor
public class CandidateApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Thông tin vị trí ứng tuyển ──
    @Column(name = "vacancy_id", nullable = false)
    private Long vacancyId;

    @Column(name = "vacancy_title", nullable = false, length = 255)
    private String vacancyTitle;

    // ── Thông tin ứng viên ──
    @Column(name = "applicant_name", nullable = false, length = 100)
    private String applicantName;

    @Column(name = "applicant_email", nullable = false, length = 100)
    private String applicantEmail;

    @Column(name = "applicant_phone", length = 20)
    private String applicantPhone;

    /** Đường dẫn file CV đã upload (lưu kết quả trả về từ /api/upload) */
    @Column(name = "resume_url", nullable = false, length = 512)
    private String resumeUrl;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @PrePersist
    protected void onCreate() {
        this.appliedAt = LocalDateTime.now();
    }
}
