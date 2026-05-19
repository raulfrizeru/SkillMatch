package com.licenta.skillmatch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_job_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateJobScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPost jobPost;

    // Scorurile calculate
    @Column(name = "semantic_score")
    private Double semanticScore;

    @Column(name = "skill_score")
    private Double skillScore;

    @Column(name = "domain_score")
    private Double domainScore;

    @Column(name = "experience_score")
    private Double experienceScore;

    @Column(name = "soft_skills_score")
    private Double softSkillsScore;

    @Column(name = "interview_score")
    private Double interviewScore;

    @Column(name = "final_score")
    private Double finalScore;

    // Datele extrase
    @Column(name = "extracted_cv_json", columnDefinition = "LONGTEXT")
    private String extractedCvJson;

    @Column(name = "extracted_job_json", columnDefinition = "LONGTEXT")
    private String extractedJobJson;

    @Column(name = "cv_ocr_text", columnDefinition = "LONGTEXT")
    private String cvOcrText;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

