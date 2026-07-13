package com.example.demo.repository;

import com.example.demo.entity.MilestoneMutationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneMutationLogRepository extends JpaRepository<MilestoneMutationLog, Long> {

    /** Lấy lịch sử biến động của một cột mốc cụ thể, mới nhất trước */
    List<MilestoneMutationLog> findByMilestoneIdOrderByPerformedAtDesc(Long milestoneId);

    /** Lấy toàn bộ lịch sử biến động của một dự án (tất cả milestones) */
    List<MilestoneMutationLog> findByProjectIdOrderByPerformedAtDesc(Long projectId);
}
