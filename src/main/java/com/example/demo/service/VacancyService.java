package com.example.demo.service;

import com.example.demo.entity.CandidateApplication;
import com.example.demo.entity.enums.ApplicationStatus;
import com.example.demo.repository.CandidateApplicationRepository;
import com.example.demo.annotation.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VacancyService {

    private final CandidateApplicationRepository applicationRepository;

    // ── F_38: Apply & HR Dashboard ────────────────────────────────────────────

    /** Save candidate application — auto-status PENDING via entity default */
    @Auditable(action = "CREATE", table = "candidate_applications")
    public CandidateApplication submitApplication(CandidateApplication application) {
        return applicationRepository.save(application);
    }

    /** HR: All applications, newest first */
    public List<CandidateApplication> getAllApplications() {
        return applicationRepository.findAllByOrderByAppliedAtDesc();
    }

    /** HR: Filter applications by vacancy */
    public List<CandidateApplication> getApplicationsByVacancy(Long vacancyId) {
        return applicationRepository.findByVacancyIdOrderByAppliedAtDesc(vacancyId);
    }

    /** HR: Filter applications by status */
    public List<CandidateApplication> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatusOrderByAppliedAtDesc(status);
    }

    /** HR: Filter applications by vacancy AND status */
    public List<CandidateApplication> getApplicationsByVacancyAndStatus(Long vacancyId, ApplicationStatus status) {
        return applicationRepository.findByVacancyIdAndStatusOrderByAppliedAtDesc(vacancyId, status);
    }

    /** Candidate: Get own applications */
    public List<CandidateApplication> getApplicationsByEmail(String email) {
        return applicationRepository.findByApplicantEmailOrderByAppliedAtDesc(email);
    }

    /** Candidate: Update their own application (only if PENDING) */
    @Auditable(action = "UPDATE", table = "candidate_applications")
    public CandidateApplication updateMyApplication(Long id, String email, CandidateApplication updatedData) {
        CandidateApplication app = applicationRepository.findByIdAndApplicantEmail(id, email)
                .orElseThrow(() -> new RuntimeException("Application not found or you don't have permission."));
        
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("You can only edit applications that are currently PENDING.");
        }

        app.setApplicantName(updatedData.getApplicantName());
        app.setApplicantPhone(updatedData.getApplicantPhone());
        app.setCoverLetter(updatedData.getCoverLetter());
        
        if (updatedData.getResumeUrl() != null && !updatedData.getResumeUrl().isBlank()) {
            app.setResumeUrl(updatedData.getResumeUrl());
        }

        return applicationRepository.save(app);
    }

    /** Candidate: Delete their own application (only if PENDING) */
    @Auditable(action = "DELETE", table = "candidate_applications")
    public void deleteMyApplication(Long id, String email) {
        CandidateApplication app = applicationRepository.findByIdAndApplicantEmail(id, email)
                .orElseThrow(() -> new RuntimeException("Application not found or you don't have permission."));
        
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("You can only delete applications that are currently PENDING.");
        }

        applicationRepository.delete(app);
    }

    // ── F_38: HR Status Pipeline ───────────────────────────────────────────────

    /**
     * HR updates the recruitment status of a candidate application.
     * Valid transitions: PENDING → INTERVIEW → APPROVED | REJECTED
     * Any stage can also be set directly to REJECTED.
     * This action is audited automatically via @Auditable.
     */
    @Auditable(action = "UPDATE", table = "candidate_applications")
    public CandidateApplication updateApplicationStatus(Long id, ApplicationStatus newStatus) {
        CandidateApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
        app.setStatus(newStatus);
        return applicationRepository.save(app);
    }
}
