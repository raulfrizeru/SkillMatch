package com.licenta.skillmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateScoreStatsDto {
    private String jobTitle;
    private String companyName;
    private Double score;
    private Boolean isMax;
    private Boolean isMin;
}

