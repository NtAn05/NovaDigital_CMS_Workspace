package com.example.demo.entity.enums;

/**
 * Represents the lifecycle states of a Project Milestone.
 * Used by ProjectMilestone entity and MilestoneSyncRequest DTO.
 */
public enum MilestoneStatus {
    PENDING,      // Not started
    IN_PROGRESS,  // In progress
    COMPLETED,    // Completed
    DELAYED       // Delayed
}
