package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class ResourceMatrixResponse {
    private String actorMode;
    private String actorName;
    private LocalDate focusDate;
    private Long selectedProjectId;
    private List<ResourceProjectOption> projects;
    private List<ResourceTaskOption> tasks;
    private List<ResourceStaffRow> staff;
    private List<ResourceAllocationResponse> allocations;
}
