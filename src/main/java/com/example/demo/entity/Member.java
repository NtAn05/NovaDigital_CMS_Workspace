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
    private String role; // Ví dụ: "PROJECT LEADER", "MEMBER"

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl; // Đường dẫn ảnh đại diện thành viên

    @Column(name = "facebook_url", length = 255)
    private String facebookUrl;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;
}
