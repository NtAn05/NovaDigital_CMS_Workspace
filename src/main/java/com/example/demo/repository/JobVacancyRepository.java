package com.example.demo.repository;

import com.example.demo.entity.JobVacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobVacancyRepository extends JpaRepository<JobVacancy, Long> {

    /**
     * Truy vấn lấy danh sách các tin tuyển dụng đang mở.
     * Được gọi bởi VacancyService để phục vụ API GET /api/vacancies.
     */
    List<JobVacancy> findByStatus(JobVacancy.VacancyStatus status);
}
