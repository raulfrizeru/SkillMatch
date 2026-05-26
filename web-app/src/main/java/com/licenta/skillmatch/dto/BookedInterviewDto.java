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
public class BookedInterviewDto {
    private Long id;
    private Long jobApplicationId;
    private LocalDateTime interviewDateTime;
    private Double interviewScore;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}

