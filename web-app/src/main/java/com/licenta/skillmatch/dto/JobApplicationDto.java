package com.licenta.skillmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplicationDto {
    private Long id;
    private String jobTitle;
    private String companyName;
    private String jobDescription;
    private LocalDateTime applyDate;
    private String status;
    private Double finalScore;
    private Double semanticScore;
    private Double skillsScore;
    private Double experienceScore;
    private Double domainScore;
    private Double softSkillsScore;
    private Double interviewScore;
    private Long jobPostId;
    private String candidateName;
    private String candidateEmail;
    private String candidateCvPath;
}
