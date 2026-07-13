package com.example.demo.dto;

import com.example.demo.entity.enums.MilestoneStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for the Sync Endpoint:
 *   PUT /api/projects/{projectId}/milestones/{id}/sync
 *
 * This DTO is intentionally minimal — it only carries the fields
 * that are allowed to change during a sync operation (progress + status).
 * "performedBy" is derived from the JWT Security Context, not from this payload.
 */
@Data
@NoArgsConstructor
public class MilestoneSyncRequest {

    /**
     * Progress percentage. Must be strictly between 0 and 100 (inclusive).
     * Validates boundary conditions to prevent PM from entering invalid values
     * (e.g., dragging slider to 101% or typing -5%).
     */
    @NotNull(message = "Progress percentage is required.")
    @Min(value = 0, message = "Progress percentage cannot be less than 0%.")
    @Max(value = 100, message = "Progress percentage cannot exceed 100%.")
    private Integer progressPercentage;

    /**
     * New milestone status.
     * Must match one of: PENDING, IN_PROGRESS, COMPLETED, DELAYED.
     */
    @NotNull(message = "Status is required.")
    private MilestoneStatus status;
}
