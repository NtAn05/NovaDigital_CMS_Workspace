package com.example.demo.repository;

import com.example.demo.entity.ProjectAssignment;
import com.example.demo.entity.ProjectAssignment.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {

    /** All assignments for a given project (used by Admin panel). */
    List<ProjectAssignment> findByProjectId(Long projectId);

    /** All projects a given user is assigned to. */
    List<ProjectAssignment> findByUserId(Long userId);

    /** All projects a user is assigned to with a specific role. */
    List<ProjectAssignment> findByUserIdAndProjectRole(Long userId, ProjectRole role);

    /** Find a specific assignment (user on project). */
    Optional<ProjectAssignment> findByProjectIdAndUserId(Long projectId, Long userId);

    /** Check if a user is PM on a specific project. */
    boolean existsByProjectIdAndUserIdAndProjectRole(Long projectId, Long userId, ProjectRole role);

    /** Remove a user's assignment from a project. */
    void deleteByProjectIdAndUserId(Long projectId, Long userId);
}
