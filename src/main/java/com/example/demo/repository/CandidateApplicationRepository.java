package com.example.demo.repository;

import com.example.demo.entity.CandidateApplication;
import com.example.demo.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CandidateApplicationRepository extends JpaRepository<CandidateApplication, Long> {

    /** HR views all applications — sorted newest first */
    List<CandidateApplication> findAllByOrderByAppliedAtDesc();

    /** HR filter by specific vacancy */
    List<CandidateApplication> findByVacancyIdOrderByAppliedAtDesc(Long vacancyId);

    /** HR filter by recruitment pipeline status */
    List<CandidateApplication> findByStatusOrderByAppliedAtDesc(ApplicationStatus status);

    /** HR filter by vacancy AND status */
    List<CandidateApplication> findByVacancyIdAndStatusOrderByAppliedAtDesc(Long vacancyId, ApplicationStatus status);

    /** Candidate fetches their own applications */
    List<CandidateApplication> findByApplicantEmailOrderByAppliedAtDesc(String applicantEmail);

    /** Security check: find application by ID and owner's email */
    java.util.Optional<CandidateApplication> findByIdAndApplicantEmail(Long id, String applicantEmail);
}
