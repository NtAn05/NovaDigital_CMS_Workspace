package com.example.demo.repository;

import com.example.demo.entity.ProjectMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMilestoneRepository extends JpaRepository<ProjectMilestone, Long> {

    /** Lấy tất cả milestones thuộc một dự án, sắp xếp theo ngày tạo tăng dần */
    List<ProjectMilestone> findByProjectIdOrderByCreatedAtAsc(Long projectId);

    /** Kiểm tra xem dự án đã có milestone trùng tên chưa (tránh duplicate) */
    Optional<ProjectMilestone> findByProjectIdAndName(Long projectId, String name);

    /** Xóa tất cả milestones của một dự án (dùng khi xóa Project) */
    void deleteAllByProjectId(Long projectId);
}
