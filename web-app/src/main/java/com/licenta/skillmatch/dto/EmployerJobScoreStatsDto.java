package com.licenta.skillmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerJobScoreStatsDto {
    private Long jobId;
    private String jobTitle;
    private Double averageScore;
    private Double maxScore;
    private Double minScore;
    private Double medianScore;
    private Integer totalApplicants;
}

