package com.example.demo.repository;

import com.example.demo.entity.MilestoneMutationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneMutationLogRepository extends JpaRepository<MilestoneMutationLog, Long> {

    /** Get mutation history of a specific milestone, newest first */
    List<MilestoneMutationLog> findByMilestoneIdOrderByPerformedAtDesc(Long milestoneId);

    /** Get all mutation history of a project (all milestones) */
    List<MilestoneMutationLog> findByProjectIdOrderByPerformedAtDesc(Long projectId);
}
