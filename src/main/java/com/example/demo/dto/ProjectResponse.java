package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String imageUrl;
    private String technologies;
    private Long clientId;
    private String clientName;
    private String clientEmail;

    public ProjectResponse(Long id, String title, String description, String category, String imageUrl, String technologies) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.technologies = technologies;
    }
}

