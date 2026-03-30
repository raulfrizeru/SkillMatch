package com.licenta.skillmatch.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "job_posts")
public class JobPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String extractedJobJson;

    @Column(nullable = false)
    private boolean isActive;

    @ManyToOne
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;

}
