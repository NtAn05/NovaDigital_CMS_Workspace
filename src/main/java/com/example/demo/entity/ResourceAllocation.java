package com.example.demo.entity;

import com.example.demo.entity.enums.AllocationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * UC-14: assigns an internal staff member to a project or to one concrete
 * project milestone/task for a percentage of their available capacity.
 */
@Entity
@Table(name = "resource_allocations",
        indexes = {
                @Index(name = "idx_ra_project", columnList = "project_id"),
                @Index(name = "idx_ra_user_dates", columnList = "user_id,start_date,end_date"),
                @Index(name = "idx_ra_milestone", columnList = "milestone_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** Null means a project-level allocation rather than a task-level one. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_id")
    private ProjectMilestone milestone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "allocation_percentage", nullable = false)
    private Integer allocationPercentage;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AllocationStatus status = AllocationStatus.PLANNED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "assigned_by", nullable = false, length = 100)
    private String assignedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = AllocationStatus.PLANNED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
