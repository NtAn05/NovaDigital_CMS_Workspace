package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * F_38 — Stores applicant information submitted for a job vacancy.
 * Does not create a hard FK to users table as applicants may not have an account.
 */
@Entity
@Table(name = "candidate_applications")
@Data
@NoArgsConstructor
public class CandidateApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Job Vacancy Information ──
    @Column(name = "vacancy_id", nullable = false)
    private Long vacancyId;

    @Column(name = "vacancy_title", nullable = false, length = 255)
    private String vacancyTitle;

    // ── Candidate Information ──
    @Column(name = "applicant_name", nullable = false, length = 100)
    private String applicantName;

    @Column(name = "applicant_email", nullable = false, length = 100)
    private String applicantEmail;

    @Column(name = "applicant_phone", length = 20)
    private String applicantPhone;

    /** Path to uploaded CV file (stores result returned from /api/upload) */
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
