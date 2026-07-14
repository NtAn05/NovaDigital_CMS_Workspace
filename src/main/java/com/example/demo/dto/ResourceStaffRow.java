package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ResourceStaffRow {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String avatarUrl;
    private List<String> skills;
    private Integer skillMatchPercentage;
    private Integer currentWorkload;
    private Integer currentProjectWorkload;
    private Integer availablePercentage;
    private Boolean overallocated;
    private Boolean projectAssigned;
}
