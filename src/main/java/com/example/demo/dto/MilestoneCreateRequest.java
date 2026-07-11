package com.example.demo.dto;

import com.example.demo.entity.enums.MilestoneStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for creating a new ProjectMilestone:
 *   POST /api/projects/{projectId}/milestones
 */
@Data
@NoArgsConstructor
public class MilestoneCreateRequest {

    @NotBlank(message = "Milestone name cannot be blank.")
    @Size(max = 255, message = "Milestone name must not exceed 255 characters.")
    private String name;

    private String description;

    /** Initial status — defaults to PENDING if not provided */
    private MilestoneStatus status = MilestoneStatus.PENDING;

    @Min(value = 0, message = "Progress percentage cannot be less than 0%.")
    @Max(value = 100, message = "Progress percentage cannot exceed 100%.")
    private Integer progressPercentage = 0;

    /** Optional deadline for this milestone phase */
    private LocalDate dueDate;
}
