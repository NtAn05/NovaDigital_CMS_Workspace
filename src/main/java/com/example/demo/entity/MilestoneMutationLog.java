package com.example.demo.entity;

import com.example.demo.entity.enums.MutationActionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Immutable audit log record for every data mutation on a ProjectMilestone.
 *
 * One sync update that changes both "status" and "progressPercentage"
 * will produce TWO MilestoneMutationLog rows — one per changed field.
 * This gives full, granular traceability for the Audit Trail feature.
 *
 * Note: This entity is NEVER updated after creation (append-only log).
 */
@Entity
@Table(name = "milestone_mutation_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneMutationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── References ────────────────────────────────────────
    @Column(name = "milestone_id", nullable = false)
    private Long milestoneId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    // ── Mutation Details ──────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private MutationActionType actionType; // E.g., SYNC_UPDATE, CREATE

    @Column(name = "field_name", length = 50)
    private String fieldName; // E.g., "progressPercentage", "status"

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue; // Snapshot of value before change

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // Snapshot of value after change

    // ── Audit Identity ────────────────────────────────────
    /**
     * Username extracted from Spring Security Context at time of mutation.
     * Guarantees that the author cannot be spoofed via request payload.
     */
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(name = "performed_at", updatable = false)
    private LocalDateTime performedAt;

    @PrePersist
    protected void onCreate() {
        this.performedAt = LocalDateTime.now();
    }
}
