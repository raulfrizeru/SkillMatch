package com.licenta.skillmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateJobScoreDto {
    private Long id;
    private Double semanticScore;
    private Double skillScore;
    private Double experienceScore;
    private Double domainScore;
    private Double softSkillsScore;
    private Double interviewScore;
    private Double finalScore;
    private Long candidateId;
    private Long jobPostId;
}
