package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponse {
    private Long id;
    private String name;
    private String role;
    private String avatarUrl;
    private String facebookUrl;
    private String githubUrl;
    private String linkedinUrl;
    private String skills;
    private String projectsWorked;
}
