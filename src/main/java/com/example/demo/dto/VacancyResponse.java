package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO returning job vacancy information for the Frontend.
 * Exposes only necessary fields, not the entire Entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacancyResponse {
    private Long id;
    private String title;
    private String description;
    private String workstream;
    private String location;
    private String jobType;
}
