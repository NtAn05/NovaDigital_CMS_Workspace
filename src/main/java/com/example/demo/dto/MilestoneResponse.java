package com.example.demo.dto;

import com.example.demo.entity.ProjectMilestone;
import com.example.demo.entity.enums.MilestoneStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO returned after any milestone read, create, or sync operation.
 * This is also embedded inside MilestoneBroadcastPayload for SSE events.
 *
 * Deliberately avoids exposing the full Project entity to prevent circular
 * serialization and over-fetching of data.
 */
@Data
@NoArgsConstructor
public class MilestoneResponse {

    private Long id;
    private Long projectId;
    private String projectTitle;
    private String name;
    private String description;
    private MilestoneStatus status;
    private Integer progressPercentage;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Factory method — converts entity to response DTO */
    public static MilestoneResponse from(ProjectMilestone m) {
        MilestoneResponse dto = new MilestoneResponse();
        dto.setId(m.getId());
        dto.setProjectId(m.getProject().getId());
        dto.setProjectTitle(m.getProject().getTitle());
        dto.setName(m.getName());
        dto.setDescription(m.getDescription());
        dto.setStatus(m.getStatus());
        dto.setProgressPercentage(m.getProgressPercentage());
        dto.setDueDate(m.getDueDate());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        return dto;
    }
}
