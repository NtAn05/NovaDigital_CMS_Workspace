package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ResourceAllocationResponse {
    private Long id;
    private Long projectId;
    private String projectTitle;
    private Long milestoneId;
    private String milestoneName;
    private Long userId;
    private String username;
    private String fullName;
    private Integer allocationPercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String notes;
    private String assignedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
