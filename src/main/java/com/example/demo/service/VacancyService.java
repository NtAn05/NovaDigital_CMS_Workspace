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

    /** Trả Map<workstream, List<VacancyResponse>> — dùng cho Careers Board */
    @Cacheable(cacheNames = "vacancies")
    public Map<String, List<VacancyResponse>> getActiveVacanciesGroupedByWorkstream() {
        return jobVacancyRepository.findByStatus(JobVacancy.VacancyStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.groupingBy(VacancyResponse::getWorkstream));
    }

    /** Trả List phẳng — dùng cho dropdown trong form Apply */
    public List<VacancyResponse> getAllActiveVacancies() {
        return jobVacancyRepository.findByStatus(JobVacancy.VacancyStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── F_38: Apply & HR Dashboard ────────────────────────────────────────────

    /** Lưu hồ sơ ứng tuyển */
    public CandidateApplication submitApplication(CandidateApplication application) {
        // Lấy title snapshot (tránh orphan khi vacancy bị xóa sau này)
        jobVacancyRepository.findById(application.getVacancyId())
                .ifPresent(v -> application.setVacancyTitle(v.getTitle()));
        return applicationRepository.save(application);
    }

    /** HR: Tất cả hồ sơ, mới nhất lên đầu */
    public List<CandidateApplication> getAllApplications() {
        return applicationRepository.findAllByOrderByAppliedAtDesc();
    }

    /** HR: Lọc hồ sơ theo vị trí */
    public List<CandidateApplication> getApplicationsByVacancy(Long vacancyId) {
        return applicationRepository.findByVacancyIdOrderByAppliedAtDesc(vacancyId);
    }

    /** Xóa cache vacancies khi có thay đổi dữ liệu */
    @CacheEvict(cacheNames = "vacancies", allEntries = true)
    public void evictVacanciesCache() { /* Spring AOP xử lý */ }

    // ── Mapper private (tránh lặp code) ──────────────────────────────────────

    private VacancyResponse toResponse(JobVacancy v) {
        return new VacancyResponse(v.getId(), v.getTitle(), v.getDescription(),
                v.getWorkstream(), v.getLocation(), v.getJobType());
    }
}
