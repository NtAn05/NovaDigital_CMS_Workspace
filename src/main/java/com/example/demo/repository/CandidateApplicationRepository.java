package com.example.demo.repository;

import com.example.demo.entity.CandidateApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CandidateApplicationRepository extends JpaRepository<CandidateApplication, Long> {

    /** HR views all applications — sorted newest first */
    List<CandidateApplication> findAllByOrderByAppliedAtDesc();

    /** HR filter by specific vacancy */
    List<CandidateApplication> findByVacancyIdOrderByAppliedAtDesc(Long vacancyId);
}
