package com.example.demo.repository;

import com.example.demo.entity.JobVacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobVacancyRepository extends JpaRepository<JobVacancy, Long> {

    /**
     * Query to retrieve the list of active job vacancies.
     * Called by VacancyService to serve API GET /api/vacancies.
     */
    List<JobVacancy> findByStatus(JobVacancy.VacancyStatus status);
}
