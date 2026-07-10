package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Links a client user (ROLE_USER) to a Project they have hired.
 * Clients can view milestones and receive realtime SSE updates for their projects.
 */
@Entity
@Table(name = "project_clients",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "hired_at")
    private LocalDateTime hiredAt;

    @PrePersist
    protected void onCreate() {
        this.hiredAt = LocalDateTime.now();
    }
}
