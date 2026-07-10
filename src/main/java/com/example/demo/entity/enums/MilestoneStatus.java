package com.example.demo.entity.enums;

/**
 * Represents the lifecycle states of a Project Milestone.
 * Used by ProjectMilestone entity and MilestoneSyncRequest DTO.
 */
public enum MilestoneStatus {
    PENDING,      // Chưa bắt đầu
    IN_PROGRESS,  // Đang thực hiện
    COMPLETED,    // Hoàn thành
    DELAYED       // Bị trễ hạn
}
