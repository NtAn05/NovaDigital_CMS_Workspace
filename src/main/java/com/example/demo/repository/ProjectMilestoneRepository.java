package com.example.demo.repository;

import com.example.demo.entity.ProjectMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMilestoneRepository extends JpaRepository<ProjectMilestone, Long> {

    /** Get all milestones belonging to a project, ordered by creation date ascending */
    List<ProjectMilestone> findByProjectIdOrderByCreatedAtAsc(Long projectId);

    /** Check if project already has a milestone with duplicate name (prevent duplicate) */
    Optional<ProjectMilestone> findByProjectIdAndName(Long projectId, String name);

    /** Delete all milestones of a project (used when deleting Project) */
    void deleteAllByProjectId(Long projectId);
}
