package com.example.demo.service;

import com.example.demo.dto.VacancyResponse;
import com.example.demo.entity.CandidateApplication;
import com.example.demo.entity.JobVacancy;
import com.example.demo.repository.CandidateApplicationRepository;
import com.example.demo.repository.JobVacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VacancyService {

    private final JobVacancyRepository jobVacancyRepository;
    private final CandidateApplicationRepository applicationRepository;

    // ── F_37: Careers Board ────────────────────────────────────────────────────

    /** Returns Map<workstream, List<VacancyResponse>> — used for Careers Board */
    @Cacheable(cacheNames = "vacancies")
    public Map<String, List<VacancyResponse>> getActiveVacanciesGroupedByWorkstream() {
        return jobVacancyRepository.findByStatus(JobVacancy.VacancyStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.groupingBy(VacancyResponse::getWorkstream));
    }

    /** Returns flat List — used for dropdown in Apply form */
    public List<VacancyResponse> getAllActiveVacancies() {
        return jobVacancyRepository.findByStatus(JobVacancy.VacancyStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── F_38: Apply & HR Dashboard ────────────────────────────────────────────

    /** Save candidate application */
    public CandidateApplication submitApplication(CandidateApplication application) {
        // Retrieve title snapshot (prevents orphan if vacancy is deleted later)
        jobVacancyRepository.findById(application.getVacancyId())
                .ifPresent(v -> application.setVacancyTitle(v.getTitle()));
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

    /** Evict vacancies cache when data changes */
    @CacheEvict(cacheNames = "vacancies", allEntries = true)
    public void evictVacanciesCache() { /* Handled by Spring AOP */ }

    // ── Private mapper (prevents code duplication) ──────────────────────────────────────

    private VacancyResponse toResponse(JobVacancy v) {
        return new VacancyResponse(v.getId(), v.getTitle(), v.getDescription(),
                v.getWorkstream(), v.getLocation(), v.getJobType());
    }
}
