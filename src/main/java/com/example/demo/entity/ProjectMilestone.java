package com.example.demo.entity;

import com.example.demo.entity.enums.MilestoneStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single trackable milestone (checkpoint) within a Project.
 * A milestone tracks a named phase with a status, progress percentage, and deadline.
 *
 * Relationships:
 *   - Many milestones belong to one Project (@ManyToOne)
 *
 * Lifecycle hooks auto-populate createdAt and updatedAt.
 */
@Entity
@Table(name = "project_milestones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationship ──────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // ── Core Fields ───────────────────────────────────────
    @Column(nullable = false, length = 255)
    private String name; // E.g., "Phase 1: UI Design"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MilestoneStatus status = MilestoneStatus.PENDING;

    /**
     * Progress percentage: must be between 0 and 100.
     * Validated at Controller layer via @Min(0) / @Max(100).
     */
    @Column(name = "progress_percentage", nullable = false)
    private Integer progressPercentage = 0;

    @Column(name = "due_date")
    private LocalDate dueDate;

    // ── Audit Timestamps ──────────────────────────────────
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
