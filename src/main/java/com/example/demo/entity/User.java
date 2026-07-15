package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(length = 255)
    private String password; // Stores BCrypt hashed password (null cho user đăng nhập Google)

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 2048)
    private String avatarUrl;

    @Column(name = "facebook_url", length = 255)
    private String facebookUrl;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "skills", length = 1000)
    private String skills;

    @Column(name = "projects_worked", length = 1000)
    private String projectsWorked;

    // ── Google OAuth2 fields ──
    @Column(length = 20)
    private String provider = "LOCAL"; // LOCAL hoặc GOOGLE

    @Column(name = "provider_id", length = 255)
    private String providerId; // Google Subject ID (sub)

    @Column(nullable = false, length = 20)
    private String role = "ROLE_USER"; // Default registered role is ROLE_USER

    @Column(nullable = false)
    private boolean enabled = true; // Is account active or locked

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.role == null) {
            this.role = "ROLE_USER";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
