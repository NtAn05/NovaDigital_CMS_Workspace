package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(length = 255)
    private String password;

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

    @Column(length = 20)
    private String provider = "LOCAL";

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(nullable = false, length = 20)
    private String role = "ROLE_USER";

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User() {}

    // ── Getters & Setters ──────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public String getUsername()                  { return username; }
    public void   setUsername(String username)   { this.username = username; }

    public String getPassword()                  { return password; }
    public void   setPassword(String password)   { this.password = password; }

    public String getFullName()                  { return fullName; }
    public void   setFullName(String fullName)   { this.fullName = fullName; }

    public String getEmail()                     { return email; }
    public void   setEmail(String email)         { this.email = email; }

    public String getPhone()                     { return phone; }
    public void   setPhone(String phone)         { this.phone = phone; }

    public String getAvatarUrl()                 { return avatarUrl; }
    public void   setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getFacebookUrl()               { return facebookUrl; }
    public void   setFacebookUrl(String v)       { this.facebookUrl = v; }

    public String getGithubUrl()                 { return githubUrl; }
    public void   setGithubUrl(String v)         { this.githubUrl = v; }

    public String getLinkedinUrl()               { return linkedinUrl; }
    public void   setLinkedinUrl(String v)       { this.linkedinUrl = v; }

    public String getSkills()                    { return skills; }
    public void   setSkills(String v)            { this.skills = v; }

    public String getProjectsWorked()            { return projectsWorked; }
    public void   setProjectsWorked(String v)    { this.projectsWorked = v; }

    public String getProvider()                  { return provider; }
    public void   setProvider(String provider)   { this.provider = provider; }

    public String getProviderId()                { return providerId; }
    public void   setProviderId(String v)        { this.providerId = v; }

    public String getRole()                      { return role; }
    public void   setRole(String role)           { this.role = role; }

    public boolean isEnabled()                   { return enabled; }
    public void    setEnabled(boolean enabled)   { this.enabled = enabled; }

    public LocalDateTime getLastLogin()          { return lastLogin; }
    public void          setLastLogin(LocalDateTime v) { this.lastLogin = v; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.role == null) this.role = "ROLE_USER";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
