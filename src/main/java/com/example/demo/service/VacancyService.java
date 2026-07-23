package com.example.demo.service;

import com.example.demo.entity.CandidateApplication;
import com.example.demo.repository.CandidateApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VacancyService {

    private final CandidateApplicationRepository applicationRepository;

    // ── F_38: Apply & HR Dashboard ────────────────────────────────────────────

    /** Save candidate application */
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
}
