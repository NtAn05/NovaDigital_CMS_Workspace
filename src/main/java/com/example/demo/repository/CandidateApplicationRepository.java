package com.example.demo.repository;

import com.example.demo.entity.CandidateApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CandidateApplicationRepository extends JpaRepository<CandidateApplication, Long> {

    /** HR xem tất cả hồ sơ — sắp xếp mới nhất lên đầu */
    List<CandidateApplication> findAllByOrderByAppliedAtDesc();

    /** HR filter theo vị trí cụ thể */
    List<CandidateApplication> findByVacancyIdOrderByAppliedAtDesc(Long vacancyId);
}
