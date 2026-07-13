package com.example.demo.repository;

import com.example.demo.entity.ProjectClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectClientRepository extends JpaRepository<ProjectClient, Long> {

    /** All clients for a given project (used by Admin panel). */
    List<ProjectClient> findByProjectId(Long projectId);

    /** All projects a given client user is linked to. */
    List<ProjectClient> findByUserId(Long userId);

    /** Find specific client-project link. */
    Optional<ProjectClient> findByProjectIdAndUserId(Long projectId, Long userId);

    /** Remove a client from a project. */
    void deleteByProjectIdAndUserId(Long projectId, Long userId);
}
