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
public class DashboardInterviewDto {
    private Long id;
    private Long jobApplicationId;
    private String candidateName;
    private String jobTitle;
    private String companyName;
    private LocalDateTime interviewDateTime;
    private Double interviewScore;
    private LocalDateTime completedAt;
    private Boolean canRate;
}

