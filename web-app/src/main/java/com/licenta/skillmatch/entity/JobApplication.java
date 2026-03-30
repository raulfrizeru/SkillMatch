package com.licenta.skillmatch.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "job_applications")
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne
    @JoinColumn(name = "job_post_id", nullable = false)
    private JobPost jobPost;

    private LocalDateTime applyDate;

    private String status = "PENDING";

    private Double finalScore;
    private Double semanticScore;
    private Double skillsScore;
    private Double experienceScore;
    private Double domainScore;
    private Double softSkillsScore;
    private Double interviewScore;
}
