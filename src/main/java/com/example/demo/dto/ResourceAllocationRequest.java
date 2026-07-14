package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ResourceAllocationRequest {

    @NotNull(message = "Staff member is required.")
    private Long userId;

    @NotNull(message = "Project is required.")
    private Long projectId;

    /** Optional. Null means a general project-level assignment. */
    private Long milestoneId;

    @NotNull(message = "Allocation percentage is required.")
    @Min(value = 1, message = "Allocation percentage must be at least 1%.")
    @Max(value = 100, message = "Allocation percentage cannot exceed 100%.")
    private Integer allocationPercentage;

    @NotNull(message = "Start date is required.")
    private LocalDate startDate;

    @NotNull(message = "End date is required.")
    private LocalDate endDate;

    @NotBlank(message = "Status is required.")
    private String status;

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters.")
    private String notes;
}
