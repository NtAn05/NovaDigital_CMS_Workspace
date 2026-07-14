package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 100)
    private String role; // Examples: "PROJECT LEADER", "MEMBER"

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl; // Path to member avatar image

    @Column(name = "facebook_url", length = 255)
    private String facebookUrl;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "skills", length = 1000)
    private String skills;

    @Column(name = "projects", length = 1000)
    private String projects;
}
