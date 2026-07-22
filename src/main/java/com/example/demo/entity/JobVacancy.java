package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_vacancies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobVacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Department / domain group (e.g., Engineering, Marketing, HR).
     * Frontend uses this field for grouped rendering on the Careers Board.
     */
    @Column(nullable = false, length = 100)
    private String workstream;

    @Column(length = 100)
    private String location;

    /** FULL_TIME | PART_TIME | REMOTE | CONTRACT */
    @Column(length = 50)
    private String jobType;

    /**
     * Status of the job vacancy posting.
     * Only postings with status = ACTIVE are displayed to Guests.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VacancyStatus status = VacancyStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum VacancyStatus {
        ACTIVE, CLOSED, DRAFT
    }
}
