package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Maps an internal team member (ROLE_MEMBER) to a Project with a specific role.
 * A member can be PM on one project and STAFF on another simultaneously.
 */
@Entity
@Table(name = "project_assignments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Role of this member on this specific project.
     * PM   — can sync milestones for this project.
     * STAFF — view-only access to milestones.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "project_role", nullable = false, length = 10)
    private ProjectRole projectRole;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
    }

    public enum ProjectRole {
        PM, STAFF
    }
}
